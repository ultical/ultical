# --- Build stage: fetch bower dependencies and bake the backend URL ----------
FROM node:14-alpine AS build

# bower resolves most packages over git, so git must be present.
RUN apk add --no-cache git && npm install -g bower

WORKDIR /web

# Install bower components first so this layer caches across source changes.
COPY bower.json ./
RUN bower install --allow-root

# Copy the rest of the frontend.
COPY . .

# config/config.js is gitignored; derive it from the template, then point the
# API at the backend. Pass the backend's public URL at build time, e.g.
#   docker build --build-arg BACKEND_URL=https://ultical-api.pqapp.dev ...
ARG BACKEND_URL=
RUN cp -n config/config.js.dist config/config.js 2>/dev/null || true; \
    if [ -n "$BACKEND_URL" ]; then \
      sed -i "s#hostname:[[:space:]]*'[^']*'#hostname: '${BACKEND_URL}'#" config/config.js; \
      echo "ultical: api.hostname set to ${BACKEND_URL}"; \
    fi

# --- Runtime stage: serve as a non-root user on :8080 ------------------------
# nginx-unprivileged runs as uid 101 and listens on a non-privileged port, which
# satisfies Runway's "numeric USER + EXPOSE" requirement out of the box.
FROM nginxinc/nginx-unprivileged:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web /usr/share/nginx/html
EXPOSE 8080
