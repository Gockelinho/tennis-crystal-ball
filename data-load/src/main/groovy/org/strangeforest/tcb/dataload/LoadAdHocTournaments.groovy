package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.TournamentFetcher.*

def sqlPool = new SqlPool()

def matchLoader = new MatchLoader(sqlPool)
matchLoader.load(fetchTournament(2016, 'Quito', 7161))
matchLoader.load(fetchTournament(2016, 'Montpellier', 375))
matchLoader.load(fetchTournament(2016, 'Sofia', 364))
matchLoader.load(fetchTournament(2016, 'Buenos_Aires', 506))
matchLoader.load(fetchTournament(2016, 'Rotterdam', 407))
matchLoader.load(fetchTournament(2016, 'Memphis', 402))
matchLoader.load(fetchTournament(2016, 'Rio_de_Janeiro', 6932))
matchLoader.load(fetchTournament(2016, 'Marseille', 496))
matchLoader.load(fetchTournament(2016, 'Delray_Beach', 499))
matchLoader.load(fetchTournament(2016, 'Dubai', 495))
matchLoader.load(fetchTournament(2016, 'Acapulco', 807))
matchLoader.load(fetchTournament(2016, 'Sao_Paulo', 533))
matchLoader.load(fetchTournament(2016, 'Indian_Wells', 404))
matchLoader.load(fetchTournament(2016, 'Miami', 403))
matchLoader.load(fetchTournament(2016, 'Monte_Carlo', 410))
matchLoader.load(fetchTournament(2016, 'Barcelona', 425))
matchLoader.load(fetchTournament(2016, 'Bucharest', 773))
matchLoader.load(fetchTournament(2016, 'Munich', 308))
matchLoader.load(fetchTournament(2016, 'Estoril', 7290))
matchLoader.load(fetchTournament(2016, 'Istanbul', 7163))
matchLoader.load(fetchTournament(2016, 'Madrid', 1536))

//ATPTennisLoader.loadAdditionalTournament(sqlPool, 'classpath:/tournaments/2016-indian-wells.xml')
