package org.strangeforest.tcb.dataload

import com.google.common.base.*

println 'Loading Tennis Data'
def stopwatch = Stopwatch.createStarted()

def sqlPool = new SqlPool()
def loader = new ATPTennisLoader()

sqlPool.withSql { sql -> LoadAdHocRankings.loadRankings(sql) }

LoadAdHocTournaments.loadTournaments(sqlPool)

new EloRatings(sqlPool).compute(save = true, fullSave = false)
sqlPool.withSql { sql -> loader.correctData(sql) }
sqlPool.withSql { sql -> loader.refreshMaterializedViews(sql) }

sqlPool.withSql { sql -> loader.vacuum(sql) }

new RecordsLoader().loadRecords()

sqlPool.withSql { sql -> loader.vacuum(sql) }

println "Tennis Data loaded in $stopwatch"