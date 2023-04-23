FROM node:alpine

RUN mkdir -p /app

WORKDIR /app

COPY kdrop-backend /app

RUN npm install

EXPOSE 3000

CMD node server.js