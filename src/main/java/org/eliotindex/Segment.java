package org.eliotindex;

import java.util.ArrayList;
import java.util.Scanner;

class Segment {
	static void run() {
		Scanner in = new Scanner(System.in);

		// get agency
		System.out.print("Enter agency: ");
		String agencyChoice = in.nextLine();

		// loads in EDI file with existing routes only
		ArrayList<Stop> stops = Util.getAgencyRouteStops(agencyChoice);

		int choice = 1;
		while (choice == 1) {
			System.out.print("Enter line: ");
			String lineChoice = in.nextLine();
			System.out.print("Starting stop: ");
			int startStop = in.nextInt();
			// int endStop is going to change

			// how long is the line
			int totalStops = 0; // total line length
			for (Stop stop : stops) {
				if (stop.getLine().equalsIgnoreCase(lineChoice)) {
					totalStops++;
				}
			}

			// this part gets looped
			Stop[] theLine;
			for (int a = 1; a <= totalStops; a++) // a will be endStop equivalent
			{
				int stopCount = 0;
				// check if in part of line desired
				if (a > startStop) {
					for (Stop stop : stops) {
						if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= a && stop.getOrder() >= startStop) {
							stopCount++;
						}
					}

					theLine = new Stop[stopCount];
					for (Stop stop : stops) {
						if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= a && stop.getOrder() >= startStop) {
							theLine[stop.getOrder() - startStop] = stop;
						}
					}
					// display
					System.out.println(startStop + " - " + a + ": " + Calculator.calcIndex(theLine));
				} else if (a < startStop) {
					for (Stop stop : stops) {
						if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= startStop && stop.getOrder() >= a) {
							stopCount++;
						}
					}

					theLine = new Stop[stopCount];
					for (Stop stop : stops) {
						if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= startStop && stop.getOrder() >= a) {
							theLine[stop.getOrder() - a] = stop;
						}
					}
					// display
					System.out.println(a + " - " + startStop + ": " + Calculator.calcIndex(theLine));
				}
			}

			System.out.print("Enter 1 to search again: ");
			choice = in.nextInt();
			in.nextLine(); // absorb enter
		}
	}
}