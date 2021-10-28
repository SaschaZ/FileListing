#/bin/bash

export U_ID=1000
export G_ID=1000

docker-compose down && \
  docker-compose build --force-rm --no-cache && \
  docker-compose up -d && \
  docker-compose logs -f
