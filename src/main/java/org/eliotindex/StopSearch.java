package org.eliotindex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class StopSearch {
	static void run() {
		System.out.println("Eliot Deviation Index");
		System.out.println("Stop Database");
		System.out.println("-----------------------");

		System.out.println();

		System.out.println("INSTRUCTIONS");
		System.out.println("- To search one agency, enter the agency code as listed on the stop listing webpage, https://www.eliotindex.org/stops.html.");
		System.out.println("- To search the global database, enter \"global\".");
		System.out.println("- Program will list all terms containing the contents entered, and display stop ID and agency (if searching the global database).");

		System.out.println();

		Scanner s2 = new Scanner(System.in);
		ArrayList<Stop> stops = new ArrayList<>();

		System.out.print("Enter agency: ");
		String agencyChoice = s2.nextLine();

		int cont = 1;

		if (agencyChoice.equals("global")) {
			ArrayList<String> agencies = Util.getAgencyIds();

			// loop through all agencies
			for (int i = 0; i < agencies.size(); i++) {
				stops.addAll(Util.getAgencyStops(agencies.get(i)));

				System.out.println("Loaded - Agencies: " + (i + 1) + " / " + agencies.size() + " | Stops: " + stops.size());
			}
		} else {
			stops.addAll(Util.getAgencyStops(agencyChoice));
		}

		while (cont == 1) { // loop it
			ArrayList<String> results = new ArrayList<>();
			int resultCount = 0;

			System.out.print("Enter search: ");
			String search = s2.nextLine();

			for (Stop stop : stops) {
				if (stop.getName().toLowerCase().contains(search.toLowerCase())) {
					if (agencyChoice.equals("global")) {
						results.add("- " + stop.getName() + " (" + stop.getAgency() + " | " + stop.getId() + ")");
					} else {
						results.add("- " + stop.getName() + " (" + stop.getId() + ")");
					}
					resultCount++;
				}
			}

			Collections.sort(results);

			for (String result : results) {
				System.out.println(result);
			}
			System.out.println("Results: " + resultCount);

			System.out.print("Enter 1 to search again: ");
			cont = s2.nextInt();

			s2.nextLine(); // absorbed enter
		}
	}
}