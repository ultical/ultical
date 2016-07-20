package de.ultical.backend.api;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.slugify.Slugify;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;

@Path("/sitemap.xml")
public class SitemapResource {

    public static final String DOMAIN_URL = "https://www.dfv-turniere.de";

    @Inject
    DataStore dataStore;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Document createSitemap() {

        Document doc;

        List<String> locales = new ArrayList<String>();
        locales.add("de");

        LocalDate today = LocalDate.now();
        LocalDate aWeekAgo = LocalDate.now().minusDays(7);
        LocalDate aMonthAgo = LocalDate.now().minusMonths(1);
        LocalDate halfAYearAgo = LocalDate.now().minusMonths(6);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd");

        Slugify slg = this.getSlugify();

        try (AutoCloseable c = this.dataStore.getClosable()) {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            Element urlset = doc.createElementNS("http://www.sitemaps.org/schemas/sitemap/0.9", "urlset");
            doc.appendChild(urlset);

            // add static pages
            this.appendUrlElements(urlset, doc, locales, "calendar", today.format(dtf), "1", "DAILY");
            this.appendUrlElements(urlset, doc, locales, "teams", today.format(dtf), "1", "DAILY");

            // go through all events
            for (Event event : this.dataStore.getAll(Event.class)) {
                String priority = "0.3";
                String changefreq = "MONTHLY";
                String lastmod = halfAYearAgo.format(dtf);
                if (event.getEndDate().isAfter(today)) {
                    priority = "0.9";
                    changefreq = "DAILY";
                    lastmod = today.format(dtf);
                } else if (event.getEndDate().isAfter(halfAYearAgo)) {
                    priority = "0.6";
                    changefreq = "WEEKLY";
                    lastmod = today.format(dtf);
                }

                String loc = slg.slugify(this.getEventName(event)) + "--3" + event.getId();

                this.appendUrlElements(urlset, doc, locales, loc, lastmod, priority, changefreq);
            }

            // go through all editions
            for (TournamentEdition edition : this.dataStore.getAll(TournamentEdition.class)) {
                String priority = "0.7";
                String changefreq = "WEEKLY";
                String lastmod = aWeekAgo.format(dtf);

                String loc = slg.slugify(this.getEditionName(edition)) + "--2" + edition.getId();
                this.appendUrlElements(urlset, doc, locales, loc, lastmod, priority, changefreq);
            }

            // go through all formats
            for (TournamentFormat format : this.dataStore.getAll(TournamentFormat.class)) {
                String priority = "0.6";
                String changefreq = "MONTHLY";
                String lastmod = aMonthAgo.format(dtf);

                String loc = slg.slugify(format.getName()) + "--4" + format.getId();
                this.appendUrlElements(urlset, doc, locales, loc, lastmod, priority, changefreq);
            }

            // go through all teams
            for (Team team : this.dataStore.getAll(Team.class)) {
                String priority = "0.8";
                String changefreq = "WEEKLY";
                String lastmod = aWeekAgo.format(dtf);

                String loc = "teams/" + slg.slugify(team.getName()) + "--7" + team.getId();
                this.appendUrlElements(urlset, doc, locales, loc, lastmod, priority, changefreq);
            }

            return doc;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private String getEventName(Event event) {
        String output = "";

        if (event.getName().isEmpty()) {
            return this.getEditionName(event.getTournamentEdition());
        } else {
            output = event.getName();
        }

        if (event.getMatchdayNumber() != -1) {
            output += " - " + event.getMatchdayNumber() + ". ";
            output += this.getMatchdayName(event);
        }
        return output;
    }

    private String getMatchdayName(Event event) {
        String output = "";
        if (!event.getTournamentEdition().getAlternativeMatchdayName().isEmpty()) {
            output += event.getTournamentEdition().getAlternativeMatchdayName();
        } else {
            output += "Spieltag";
        }
        return output;
    }

    private String getEditionName(TournamentEdition edition) {
        String output = "";
        if (edition == null) {
            return output;
        }
        if (edition.getName().isEmpty()) {
            output = edition.getTournamentFormat().getName();
            output += " " + edition.getSeason().getYear();
        } else {
            output = edition.getName();
        }

        return output;
    }

    private void appendUrlElements(Element urlset, Document doc, List<String> locales, String loc, String lastmod,
            String priority, String changefreq) {
        for (String locale : locales) {
            urlset.appendChild(this.createUrlElement(doc, locale, loc, lastmod, priority, changefreq));
        }
    }

    private Element createUrlElement(Document doc, String locale, String loc, String lastmod, String priority,
            String changefreq) {

        Element url = doc.createElement("url");

        Element locElement = doc.createElement("loc");
        locElement.setTextContent(DOMAIN_URL + "/" + locale + "/" + loc);
        url.appendChild(locElement);

        Element lastmodElement = doc.createElement("lastmod");
        lastmodElement.setTextContent(lastmod);
        url.appendChild(lastmodElement);

        Element changefreqElement = doc.createElement("changefreq");
        changefreqElement.setTextContent(changefreq);
        url.appendChild(changefreqElement);

        Element priorityElement = doc.createElement("priority");
        priorityElement.setTextContent(priority);
        url.appendChild(priorityElement);

        return url;
    }

    private Slugify getSlugify() {
        Slugify slg = null;
        try {
            slg = new Slugify();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> customReplacements = new HashMap<String, String>();
        customReplacements.put("ö", "o");
        customReplacements.put("ä", "a");
        customReplacements.put("ü", "u");
        customReplacements.put("Ö", "o");
        customReplacements.put("Ä", "a");
        customReplacements.put("Ü", "u");
        slg.setCustomReplacements(customReplacements);
        return slg;
    }
}
