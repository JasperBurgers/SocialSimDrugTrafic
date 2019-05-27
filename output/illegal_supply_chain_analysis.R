#=====================================
# README
# 
# This file uses the usefull_functions.R file
# for the function 'getDataFromFile'
#
# Then it uses state data from the agents
#
# To build go to functions: Build First !!!!TODO WRITE THIS
#=====================================

setwd("D://Work//WorkspaceR")
source("usefull_functions.R")

#Data formatted as: "tick","Id","LocationX","LocationY","Money"
agentData     <- getDataFromFile("Legal_100_AgentState") 	#Load 'AgentState.YYYY.MMM.DD.HH_MM_SS.txt' here
relationsData <- getDataFromFile("Legal_100_RelationsData") #Load 'RelationsData.YYYY.MMM.DD.HH_MM_SS.txt' here
orderData 	  <- getDataFromFile("Legal_100_OrderState")	#Load 'OrderState.YYYY.MMM.DD.HH_MM_SS.txt' here
shipmentData <- getDataFromFile("Legal_100_ShipmentState")	#Load 'ShipmentState.YYYY.MMM.DD.HH_MM_SS.txt' here

relationsData <- addCoordinatesToRelations(agentData, relationsData);

timeTicks <- c(1,3,5,7,9,11,13,15,20,25,30,40,60,80,100); #The time ticks you want to make plots from

for (i in timeTicks) {
	tTitle <- paste("Legal Supply Chain - Tick", i, sep = "")
	plotSupplyChain(agentData[agentData$tick==i, ],
					relationsData[relationsData$tick==i, ],
					orderData[orderData$tick==i, ],
					shipmentData[shipmentData$tick==i, ],
					tTitle);
}

#=====================================
# Functions: Build first
#=====================================
plotSupplyChain <- function(pAgentData, pRelationsData, pOrderData, pShipmentData, pTitle) {
	
	plot(pAgentData$LocationX, pAgentData$LocationY, main = pTitle, xlab = "", ylab = "", xaxt="n", yaxt="n", xlim=c(0,58), ylim=c(0,50))

	points(pAgentData$LocationX, pAgentData$LocationY, col="black", pch=16)
	
	segments(x0=pRelationsData$x, y0=pRelationsData$y, x1=pRelationsData$otherX, y1=pRelationsData$otherY, col=pRelationsData$color);
	
	points(pOrderData$LocationX, pOrderData$LocationY, col="blue", pch=16)
	points(pShipmentData$LocationX, pShipmentData$LocationY, col="yellow", pch=16)
	
	axis(1, at=c(5,17,29,41,53),labels=c("P","I","W","R","C"), col.axis="black", las=0)
	
	#draw points with color
	#rm(point_1,point_2)
	#points(graph[p_solution==FALSE,2], graph[p_solution==FALSE,3], col=2, pch=16)
	#points(graph[p_solution==TRUE,2], graph[p_solution==TRUE,3], col=4, pch=16)
	#text(x=0, y=1, label=paste("f : ",count, sep =""), col = "magenta")
}

addCoordinatesToRelations <- function(pAgentData, pRelationsData) {
	
	relationsDataNew <- pRelationsData;
	x <- c();
	y <- c();
	otherX <- c();
	otherY <- c();
	color <- c();
	print(paste("Processing", nrow(pRelationsData), "rows ....."));
	for (i in 1:nrow(pRelationsData)) {
		id <- pRelationsData$Id[i]
		x <- c(x, pAgentData[pAgentData$Id==id, ]$LocationX[1]) #Getting the first location of the vector of all locations for the given agent id
		y <- c(y, pAgentData[pAgentData$Id==id, ]$LocationY[1]) 
		otherId <- pRelationsData$OtherId[i]
		otherX <- c(otherX, pAgentData[pAgentData$Id==otherId, ]$LocationX[1])
		otherY <- c(otherY, pAgentData[pAgentData$Id==otherId, ]$LocationY[1])
		color <- c(color, rgb(1 - pRelationsData$Trust[i],pRelationsData$Trust[i],0))
	}
	relationsDataNew$x <- x
	relationsDataNew$y <- y
	relationsDataNew$otherX <- otherX
	relationsDataNew$otherY <- otherY
	relationsDataNew$color <- color
	print("Done!");
	return(relationsDataNew)
}