getDataFromFile <- function(pFileName) {
  
	print(paste("Get data from file:", paste(pFileName, "txt", sep=".")))
	rData <- read.csv(paste(pFileName, "txt", sep="."), header=TRUE,sep=",")
	return(rData)
}

plotData <- function(pData, pTitle, pXLim, pYLim, pYLab, pSavePlot, pSaveName) {
	
	print(paste("Plot data:", pTitle));
	
	# Plot save
	if (pSavePlot) {
		savepdf(pSaveName);
	}
	
	# Plot init
	plot(pData[,1], pData[,2], type="l", col=getColor(1), lty=1, pch=20, xlab="Ticks", ylab=pYLab,
		 xlim=c(0, pXLim), ylim=c(0, pYLim), xaxs="r", yaxs="r"); #xaxs="i" to remove the margin between 0,0 and bottom-left corner
	
	# Plot add the rest of the data
	if (ncol(pData) >= 3) {
		for (i in 3:ncol(pData)) {
			lines(pData[,1], pData[,i], type="l", pch=20, lty=1, col=getColor(i - 1));
		}
	}
	grid();
	# Create a title
	if (pTitle != "") {
		title(main=pTitle);
	}
	
	legend(pXLim, pYLim, names(pData[1, 2:ncol(pData)]), bg="transparent", cex=1,
		   col=getColor(1:(ncol(pData) - 1)), pch=21, lty=2, seg.len = 1, xjust = 1, yjust = 1);
	
	# End save
	if (pSavePlot) {
		dev.off();
	}
}


plotDataExt <- function(pData, pLegendNames, pHoriz, pCex, pColors, pTitle, pXLim, pYLim, pYLab, pSavePlot, pSaveName) {

	print(paste("Plot data extended:", pTitle));
	
	# Plot save
	if (pSavePlot) {
		savepdf(pSaveName);
	}
	
	# Plot init
	plot(pData[,1], pData[,2], type="l", col=pColors[1], lty=1, pch=20, xlab="Ticks", ylab=pYLab,
		 xlim=c(0, pXLim), ylim=c(0, pYLim), xaxs="r", yaxs="r"); #xaxs="i" to remove the margin between 0,0 and bottom-left corner
  
	# Plot add the rest of the data
	if (ncol(pData) >= 3) {
		for (i in 3:ncol(pData)) {
			lines(pData[,1], pData[,i], type="l", pch=20, lty=1, col=pColors[i - 1]);
		}
	}
	grid();
	# Create a title
	if (pTitle != "") {
		title(main=pTitle);
	}

	legend(pXLim, pYLim, pLegendNames, bg="transparent", horiz=pHoriz, cex=pCex, col=pColors, pch=21, lty=2, seg.len = 1,
		   xjust = 1, yjust = 1);
	
	# End save
	if (pSavePlot) {
		dev.off();
	}
}

getMaxYFromData <- function(pData) {

	rMaxY = max(pData[, 2:ncol(pData)]);
	return(rMaxY);
}

savepdf <- function(file, width=16, height=10)
{
	fname <- paste(file,".pdf",sep="");
	pdf(fname, width=width/2.54, height=height/2.54, pointsize=10);
	par(mgp=c(2.2,0.45,0), tcl=-0.4, mar=c(3.3,3.6,1.1,1.1));
}

getColor <- function(pIndex) {
	return(colors()[pIndex * 5]);
}