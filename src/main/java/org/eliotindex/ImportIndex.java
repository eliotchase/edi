package org.eliotindex;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

class ImportIndex {
	static void run() {
		Scanner in = new Scanner(System.in);
		System.out.print("Enter agency code: ");
		String agency = in.nextLine();
		System.out.print("Enter import code: ");
		String importCode = in.nextLine();

		ArrayList<String> importFile = new ArrayList<>();

		try {
			Scanner s = new Scanner(new File("../gtfs/" + agency + "-export-" + importCode + ".txt"));
			int z = 0; // skip first
			while (s.hasNextLine()) {
				if (z == 0) {
					s.nextLine();
					z++;
				} else {
					String data = s.nextLine();
					importFile.add(data);
				}
			}
		} catch (Exception e) {
			System.out.println("Error - Import file not found.");
		}

		// stop import
		// load in only existing stops
		ArrayList<Stop> stops = Util.getAgencyStops(agency);

		while (!importFile.isEmpty()) {
			String code = importFile.get(0).substring(2);
			String name = importFile.get(1).substring(2);
			String branch = importFile.get(2).substring(2);

			ArrayList<Stop> custom = new ArrayList<>();

			int stopPos = 3;
			for (int i = 3; !importFile.get(i).equals("-0"); i++) {
				for (Stop stop : stops) {
					if (stop.getId().equalsIgnoreCase(importFile.get(i))) {
						custom.add(stop);
					}
				}

				stopPos = i;
			}

			Stop[] theLine = new Stop[custom.size()];
			for (int i = 0; i < custom.size(); i++) {
				theLine[i] = custom.get(i);
			}

			Calculator.saveIndex(theLine, code, name, branch, agency);

			if (stopPos > -2) {
				importFile.subList(0, stopPos + 2).clear();
			}
		}
	}
}
