library("ggplot2")
library("scales")
print("Importing data...")
roundData <- read.csv("benchmark-data/round-data.csv", header=T)
print("DONE")
print("Imported lines:")
print("Generating plots...")
print("Hint: All plots are commented out by default. Uncomment the plots you want to create.")

# ========================================================================
# Activity 0.01, 0.1, 0.5 and 1.0 for script `scripts/benchmarks/activity`
# ========================================================================
compare <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$ActivityCat <- cut(roundData$Activity, c(0, 0.01, 0.1, 0.5, 1), labels = c("1%", "10%", "50%", "100%"))
	ggplot(roundData,
		aes(ClientsCat, log(Data)/log(2))) +
		geom_boxplot(aes(colour = Algorithm)) +
		facet_wrap(~ ActivityCat) +
		labs(x="# Participants", y="Data (log)") +
		theme(axis.title.y = element_text(family="serif", 
			size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
			size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_colour_brewer(palette="Dark2") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/compare.pdf", width = 7, height = 5)

}

# ======================================================================
# required rounds for script `scripts/benchmarks/footprint_convergence
# ======================================================================
req_rounds <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000), labels=c(1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000))
	roundData$SlotsCat <- cut(roundData$Slots, c(0,1, 2, 4, 6, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	reference <- function(x) log(x)/log(2)
	roundData$RefValues <- reference(roundData$Clients)
	ggplot(subset(roundData,EmptySlots < Slots),
		aes(x=Clients), group = SlotsCat) +
		geom_line(aes(y=RefValues), color="blue", linetype="dashed", size = 2, alpha = 0.5) + 
		geom_smooth(aes(y=ReqRounds, color=SlotsCat)) + 
		labs(x="# Participants", y="Required rounds") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_colour_brewer(palette="OrRd", name="# Slots") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/req_rounds.pdf", width = 5, height = 3)
}

collisions <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$SlotsCat <- cut(roundData$Slots, c(0,1, 2, 4, 6, 8, 16, 24, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 24, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	ggplot(roundData,
		aes(SlotsCat, Collisions/Clients)) +
		geom_boxplot(aes(fill=ClientsCat)) +
		labs(x="# Slots", y="Collisions") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_fill_brewer(palette="OrRd", name="# Participants") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/collisions.pdf", width = 9, height = 5)
}

empty_slots <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$SlotsCat <- cut(roundData$Slots, c(0,1, 2, 4, 6, 8, 16, 24, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 24, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	roundData$ActivityCat <- cut(roundData$Activity, c(0, 0.01, 0.1, 0.5, 1), labels = c("1%", "10%", "50%", "100%"))
	ggplot(roundData,
		aes(Clients, EmptySlots/Slots, group = ActivityCat)) +
		geom_smooth(aes(colour = ActivityCat), alpha = 0.3, method = "loess") +
		labs(x="# Participants", y="% Empty slots") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_colour_brewer(palette="OrRd", name="Activity rate") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/empty_slots.pdf", width = 9, height = 5)
}

# ======================================================================
# trade-off between # bits and # required rounds
# for script `footprint_bit_round_tradeoff`
# ======================================================================
bit_round_tradeoff <- function() {
	roundData$BitsCat <- cut(roundData$Bits, c(1, 2, 4, 6, 8), labels=c(2, 4, 6, 8))
	roundData$SlotsCat <- cut(roundData$Slots, c(0,1, 2, 4, 6, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	ggplot(roundData,
	     aes(BitsCat, ReqRounds)) +
	     geom_boxplot(aes(fill=SlotsCat)) +
	     labs(x="# Bits per slot", y="Required rounds") +
	     theme(axis.title.y = element_text(family="serif",
	     size=12, angle=90, vjust=0.25)) +
	     theme(axis.title.x = element_text(family="serif",
	     size=12, angle=00, hjust=0.54, vjust=0)) +
	     theme(axis.text.x = element_text(family="serif")) +
	     theme(axis.text.y = element_text(family="serif")) +
	     theme(legend.text = element_text(family="serif")) +
	     theme(legend.title = element_text(family="serif")) +
	     scale_fill_brewer(palette="OrRd", name="# Slots") +
	     theme(panel.background = element_blank()) +
	     theme(axis.line = element_line(colour = "black")) +
	     theme(panel.grid.major = element_line(colour = "grey")) +
	     theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/bit_round_tradeoff.pdf", width = 5, height = 3)
}

# ======================================================================
# empty slots for different # bits and # slots
# ======================================================================
empty_slots_bits <- function() {
	roundData$BitsCat <- cut(roundData$Bits, c(1, 2, 4, 6, 8), labels=c(2, 4, 6, 8))
	roundData$SlotsCat <- cut(roundData$Slots, c(0,1, 2, 4, 6, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	ggplot(roundData,
	     aes(BitsCat, EmptySlots/Slots)) +
	     geom_boxplot(aes(fill=SlotsCat)) +
	     labs(x="# Bits per slot", y="% Empty slots") +
	     theme(axis.title.y = element_text(family="serif",
	     size=12, angle=90, vjust=0.25)) +
	     theme(axis.title.x = element_text(family="serif",
	     size=12, angle=00, hjust=0.54, vjust=0)) +
	     theme(axis.text.x = element_text(family="serif")) +
	     theme(axis.text.y = element_text(family="serif")) +
	     theme(legend.text = element_text(family="serif")) +
	     theme(legend.title = element_text(family="serif")) +
	     scale_fill_brewer(palette="OrRd", name="# Slots") +
	     theme(panel.background = element_blank()) +
	     theme(axis.line = element_line(colour = "black")) +
	     theme(panel.grid.major = element_line(colour = "grey")) +
	     theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/empty_slots_bits.pdf", width = 9, height = 5)
}

# ==============================================================
# bits per slot for script `scripts/benchmarks/footprint_size`
# ==============================================================
bits <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$BitsCat <- cut(roundData$Bits, c(0, 4, 5, 6, 7, 8), labels=c(4, 5, 6, 7, 8))
	scale_y_log2 <- function (...) 
	{
	  scale_y_continuous(..., trans = log2_trans())
	}
	ggplot(roundData,
		aes(BitsCat,
			weight = Collisions,
			fill = ClientsCat)) +
		geom_bar(position="dodge", color = "black") +
		scale_y_log2() + 
		labs(x="# bits per slot", y="Undetected collisions") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_fill_brewer(palette="OrRd", name = "# Participants") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/bits.pdf", width = 7, height = 5)
}

# ==================================================================
# Plot for Pfitzmann for script `scripts/benchmarks/pfitzmann_ratio`
# ==================================================================
pfitzmann <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$BitsCat <- cut(roundData$Bits, c(0,1, 2, 4, 6, 8, 16, 20, 24, 28, 32, 40, 48, 56, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 20, 24, 28, 32, 40, 48, 56, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	ggplot(roundData,
		aes(Clients, Data, group = BitsCat)) +
		geom_smooth(aes(colour=BitsCat), method = "loess", size = 1.2) +
		labs(x="# Participants", y="Data") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_colour_brewer(palette="OrRd", name="# Slots per client") +
		scale_fill_brewer(palette="OrRd", name="# Slots per client") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/pfitzmann.pdf", width = 5, height = 3)
}

# ==================================================================
# Plot for Chaum for script `scripts/benchmarks/chaum_ratio`
# ==================================================================
chaum <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$BitsCat <- cut(roundData$Bits, c(0,1, 2, 4, 6, 8, 16, 20, 24, 28, 32, 40, 48, 56, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 20, 24, 28, 32, 40, 48, 56, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	ggplot(roundData,
		aes(Clients, Data, group = BitsCat)) +
		geom_smooth(aes(colour=BitsCat), method = "loess", size = 1.2) +
		labs(x="# Participants", y="Data") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_colour_brewer(palette="OrRd", name="# Slots per client") +
		scale_fill_brewer(palette="OrRd", name="# Slots per client") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/chaum.pdf", width = 5, height = 3)
}

# ==================================================================
# Plot for Chaum for script `scripts/benchmarks/chaum_ratio`
# ==================================================================
chaum_collision <- function() {
	roundData$ClientsCat <- cut(roundData$Clients, c(0, 100, 200, 500, 1000, 2000, 5000, 10000), labels=c(100, 200, 500, 1000, 2000, 5000, 10000))
	roundData$BitsCat <- cut(roundData$Bits, c(0,1, 2, 4, 6, 8, 16, 20, 24, 28, 32, 40, 48, 56, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384), labels=c(1, 2, 4, 6, 8, 16, 20, 24, 28, 32, 40, 48, 56, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384))
	ggplot(roundData,
		aes(Bits, Collisions/(Clients * Activity), group = ClientsCat)) +
		geom_smooth(aes(colour=ClientsCat), method = "loess", size = 1.2, alpha = 0.3) +
		scale_x_continuous(trans = "log2", breaks = c(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096)) +
		labs(x="# Slots per participant", y="Collisions") +
		theme(axis.title.y = element_text(family="serif", 
		size=12, angle=90, vjust=0.25)) + 
		theme(axis.title.x = element_text(family="serif",
		size=12, angle=00, hjust=0.54, vjust=0)) + 
		theme(axis.text.x = element_text(family="serif")) +
		theme(axis.text.y = element_text(family="serif")) +
		theme(legend.text = element_text(family="serif")) +
		theme(legend.title = element_text(family="serif")) +
		scale_colour_brewer(palette="OrRd", name="# Participants") +
		scale_fill_brewer(palette="OrRd", name="# Participants") +
		theme(panel.background = element_blank()) +
		theme(axis.line = element_line(colour = "black")) + 
		theme(panel.grid.major = element_line(colour = "grey")) + 
		theme(panel.grid.minor = element_line(colour = "grey"))
	ggsave("plots/chaum_collision.pdf", width = 5, height = 3)
}

# ======================
# Average data for table
# ======================
average_data <- function() {
	print("0.01")
	print("Footprint")
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Footprint" & Clients==10000)$Bytes)
	print("Chaum")
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Chaum" & Clients==10000)$Bytes)
	print("Pfitzmann")
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==0.01 & Algorithm=="Pfitzmann" & Clients==10000)$Bytes)
	print("0.1")
	print("Footprint")
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Footprint" & Clients==10000)$Bytes)
	print("Chaum")
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Chaum" & Clients==10000)$Bytes)
	print("Pfitzmann")
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==0.1 & Algorithm=="Pfitzmann" & Clients==10000)$Bytes)
	print("1")
	print("Footprint")
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Footprint" & Clients==10000)$Bytes)
	print("Chaum")
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Chaum" & Clients==10000)$Bytes)
	print("Pfitzmann")
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==100)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==200)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==500)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==1000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==2000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==5000)$Bytes)
	mean(subset(reservationData,Activity==1 & Algorithm=="Pfitzmann" & Clients==10000)$Bytes)
}

make_plots <- function() {
	compare()
	# req_rounds()
	# collisions()
	# empty_slots()
	# bit_round_tradeoff()
	# empty_slots_bits()
	# bits()
	# pfitzmann()
	# chaum()
	# chaum_collision()
	# average_data()
}

make_plots()

print("DONE")

q()
