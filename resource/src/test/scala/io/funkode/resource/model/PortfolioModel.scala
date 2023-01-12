package io.funkode.resource.model

enum PortfolioEnum:
  case Network(
      id: String,
      chainId: String,
      name: String,
      currency: String,
      transactions: List[PortfolioEnum.Transaction]
  )
  case Transaction(network: PortfolioEnum.Network, hash: String, timestamp: Long)

case class Portfolio(networks: Network, tx: Transaction)
case class Network(
    id: String,
    chainId: String,
    name: String,
    currency: String,
    transactions: List[Transaction]
)

case class Transaction(
    network: Network,
    hash: String,
    timestamp: Long
)
