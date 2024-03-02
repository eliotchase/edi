package org.eliotindex;

import java.util.ArrayList;
import java.util.Scanner;

class StopInfo {
	static void run() {
		Scanner s2 = new Scanner(System.in);

		System.out.print("Enter agency: ");
		String agencyChoice = s2.nextLine();
		ArrayList<Stop> stops = Util.getAgencyStops(agencyChoice);

		int cont = 1;

    /*while (cont == 1) // loop it
    {*/
		System.out.print("Stop ID: ");
		String stopChoice = s2.nextLine();

		for (Stop stop : stops) {
			if (stop.getId().equalsIgnoreCase(stopChoice)) {
				System.out.println("Stop Name: " + stop.getName());
				System.out.println("Stop Lat: " + stop.getLat());
				System.out.println("Stop Lon: " + stop.getLon());
			}
		}

		PopulationData.run(agencyChoice, stopChoice);

      /*System.out.print("Enter 1 to search again: ");
      cont = s2.nextInt();

      s2.nextLine(); // absorbed enter
    }*/
	}
}