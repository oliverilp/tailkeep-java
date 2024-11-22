FROM golang:1.23-bookworm AS build
WORKDIR /app

COPY . .

RUN go mod tidy

RUN make build

FROM scratch
WORKDIR /app

COPY --from=build /app/bin .

EXPOSE 5000

CMD [ "/app/tailkeep_media" ]
