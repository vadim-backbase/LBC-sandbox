const PROXY_CONFIG = [
  {
    context: [
      "/api"
    ],
    target: "http://localhost:7777",
    secure: false
  }
]

module.exports = PROXY_CONFIG;
