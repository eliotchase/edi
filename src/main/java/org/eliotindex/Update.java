package org.eliotindex;

import java.io.File;
import java.io.FileWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs all the update files in one go
 */
class Update {
	static void update() {
		Generators.stopList();
		Generators.routeList();
		Generators.fullMap();
		Generators.agencyMap();
		//Generators.stopMap();
		Generators.statsList();
		Stats.generate();

		// update home page with date
		ArrayList<String> homepage = new ArrayList<>();
		try {
			Scanner s = new Scanner(new File("public/index.html"));
			while (s.hasNextLine()) {
				homepage.add(s.nextLine());
			}
		} catch (Exception e) {
			System.out.println("Error - no index.html. (how in the hell did you get this)");
		}

		DateTimeFormatter currentTimeFormat = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm z");
		ZonedDateTime now = ZonedDateTime.now();
		String currentTime = currentTimeFormat.format(now);

		homepage.set(homepage.size() - 1, "<p><b>Last Updated: </b> " + currentTime + "</p>");

		try {
			File newFile1 = new File("public/index.html");
			FileWriter fileWriter1 = new FileWriter(newFile1);

			fileWriter1.write(homepage.get(0) + "\n");

			for (int i = 1; i < homepage.size() - 1; i++) {
				fileWriter1.append(homepage.get(i)).append("\n");
			}

			fileWriter1.append(homepage.get(homepage.size() - 1));
			fileWriter1.close();
		} catch (Exception e) {
			System.out.println("Error.");
		}

		System.out.println("Update Time: " + currentTime);
	}
}