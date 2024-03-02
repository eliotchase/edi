package org.eliotindex;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

class Request {
	static void run() {
		// get request list
		ArrayList<String> requests = new ArrayList<>();
		try {
			Scanner s = new Scanner(new File("requests.txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				requests.add(data);
			}
		} catch (Exception e) {
			System.out.println("Error - no requests (odd, you always have some).");
		}

		// get agency list
		ArrayList<String> agencies = new ArrayList<>();
		try {
			Scanner s = new Scanner(new File("agencies.txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				agencies.add(data.substring(0, data.indexOf(";")));
			}
			agencies.add("zz-none"); // future agency.
		} catch (Exception e) {
			System.out.println("Error - no agencies.");
		}

		Scanner in = new Scanner(System.in);
		String agencyChoice = "no data";

		boolean actualAgency = false;
		while (!actualAgency) {
			System.out.print("Agency: ");
			agencyChoice = in.nextLine();

			for (String agency : agencies) {
				if (agency.equalsIgnoreCase(agencyChoice)) {
					actualAgency = true;
					break;
				}
			}
		}

		System.out.print("Request: ");
		String newRequest = in.nextLine();

		requests.add(agencyChoice + " - " + newRequest);

		// adds to request file
		try {
			File newFile = new File("requests.txt");
			FileWriter fileWriter = new FileWriter(newFile);

			fileWriter.write(requests.get(0) + "\n");

			for (int i = 1; i < requests.size(); i++) {
				fileWriter.append(requests.get(i)).append("\n");
			}

			fileWriter.close();

			System.out.println("Request added.");
		} catch (Exception e) {
			System.out.println("Error.");
		}
	}
}