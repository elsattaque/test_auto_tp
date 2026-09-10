# language: fr
Fonctionnalité: Distribution de butin de la guilde
  En tant que maitre de la guilde
  Je veux distribuer le butin aux membres
  Afin de recompenser les participants
  Sans jamais mettre la guilde a decouvert

  Contexte:
    Soit une guilde "guilde-du-crepuscule"

  Scénario: Le butin est distribue à un membre de la guilde
    Soit le compte de la guilde a 100 pieces d'or
    Quand je distribue 40 pieces d'or de butin
    Alors la guilde a 60 pieces d'or
    Et le compte de la guilde est enregistre

  Scénario: Le butin peut vider entierement le compte de la guilde
    Soit le compte de la guilde a 100 pieces d'or
    Quand je distribue 100 pieces d'or de butin
    Alors la guilde a 0 pieces d'or
    Et le compte de la guilde est enregistre

  Scénario: La distribution est refusee quand le compte de la guilde est insuffisant
    Soit le compte de la guilde a 100 pieces d'or
    Quand je distribue 120 pieces d'or de butin
    Alors la distribution est refusee pour fonds insuffisants
    Et la guilde a 100 pieces d'or
    Et le compte de la guilde n'est pas enregistre

  Scénario: La distribution est refusee sur un tresor vide
    Soit le compte de la guilde a 0 pieces d'or
    Quand je distribue 20 pieces d'or de butin
    Alors la distribution est refusee pour fonds insuffisants
    Et la guilde a 0 pieces d'or

  Scénario: Plusieurs membres sont servis successivement
    Soit le compte de la guilde a 100 pieces d'or
    Quand je distribue 20 pieces d'or de butin
    Et je distribue 20 pieces d'or de butin
    Alors la guilde a 60 pieces d'or

  Scénario: Un depot rend possible une distribution jusqu'a la refusee
    Soit le compte de la guilde a 100 pieces d'or
    Quand je depose 200 pieces d'or dans le compte de la guilde
    Et je distribue 250 pieces d'or de butin
    Alors la guilde a 50 pieces d'or

