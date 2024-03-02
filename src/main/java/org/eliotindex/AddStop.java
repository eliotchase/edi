package org.eliotindex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This file is different from the -1 code on Calculator — this adds a stop to the public stop listing
 */
class AddStop {
	static void add() {
		Scanner s2 = new Scanner(System.in);

		System.out.print("Enter agency: ");
		String agencyChoice = s2.nextLine();
		ArrayList<Stop> stops = Util.getAgencyStops(agencyChoice);

		int cont = 1;

		while (cont == 1) {

			System.out.print("Enter Stop ID: ");
			String newId = s2.nextLine();
			System.out.print("Enter Stop Name: ");
			String newName = s2.nextLine();
			System.out.print("Enter Stop Lat: ");
			double newLat = s2.nextDouble();
			System.out.print("Enter Stop Lon: ");
			double newLon = s2.nextDouble();

			stops.add(new Stop(newId, newName, newLat, newLon));
			System.out.println("Stop added.");

			// writes to stop file, converted in list.java
			try {
				File newFile1 = new File("stops/" + agencyChoice + ".txt");
				FileWriter fileWriter1 = new FileWriter(newFile1);

				fileWriter1.write(stops.get(0).getId() + ";" + stops.get(0).getName() + ";" + stops.get(0).getLat() + ";" + stops.get(0).getLon() + "\n");

				for (int i = 1; i < stops.size(); i++) {
					fileWriter1.append(stops.get(i).getId()).append(";").append(stops.get(i).getName()).append(";").append(String.valueOf(stops.get(i).getLat())).append(";").append(String.valueOf(stops.get(i).getLon())).append("\n");
				}

				fileWriter1.close();

				System.out.println("Complete.");
			} catch (IOException e) {
				System.out.println("Error, can't write to stop file " + agencyChoice + ".");
			}
			System.out.print("Enter 1 to add another stop: ");
			cont = s2.nextInt();
			s2.nextLine(); // enter key
			System.out.println(); // new line
		}
	}
}