package org.eliotindex;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

class CodeToName {
	static void run() {
		Scanner in = new Scanner(System.in);

		System.out.print("Enter agency: ");
		String agencyChoice = in.nextLine();

		ArrayList<String> dataLine = new ArrayList<>();
		// get edis/agency
		try {
			Scanner s = new Scanner(new File("edis/" + agencyChoice + ".txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				dataLine.add(data);
			}
		} catch (Exception e) {
			// System.out.println("Error, no EDI file (" + agencies.get(i) + ")"); // expected error if no routes in database.
		}

		// input name and branch
		for (int i = 0; i < dataLine.size(); i++) {
			System.out.println(dataLine.get(i).substring(0, dataLine.get(i).indexOf(";")));
			System.out.print("Name: ");
			String name = in.nextLine();
			System.out.print("Branch: ");
			String branch = in.nextLine();

			dataLine.set(i, dataLine.get(i) + ";" + name + ";" + branch);
		}

		// save the new file
		try {
			File newFile1 = new File("edis/" + agencyChoice + ".txt");
			FileWriter fileWriter1 = new FileWriter(newFile1);

			fileWriter1.write(dataLine.get(0) + "\n");

			for (int b = 1; b < dataLine.size(); b++) {
				fileWriter1.append(dataLine.get(b)).append("\n");
			}

			fileWriter1.close();
		} catch (Exception e) {
			System.out.println("Error.");
		}
	}
}