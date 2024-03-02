package org.eliotindex;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The thing that calculates the Eliot Deviation Index
 */
class Calculator {
	static String agencyChoice;

	static void run() {
		System.out.println("Welcome to the Eliot Deviation Index calculator!");
		System.out.println();

		System.out.println("Before starting, please select the agency you would like to access data for. Agency codes are listed online at www.eliotindex.org.");

		System.out.println();

		System.out.println("INSTRUCTIONS - EXISTING LINES");
		System.out.println("Enter line name when prompted.");

		System.out.println();

		System.out.println("INSTRUCTIONS - CUSTOM LINES");
		System.out.println("- Enter \"custom\" when prompted for the line.");
		System.out.println("- Enter the stop IDs in order. A full list can be found at https://www.eliotindex.org/stops.html.");
		System.out.println("- Enter \"-1\" to add a custom stop.");
		System.out.println("- Enter \"-2\" to remove the last stop added.");
		System.out.println("- Enter \"-3\" to add a stop from another agency.");
		System.out.println("- Enter \"-0\" after the last stop is added.");
		System.out.println("- Enter \"segment\" to add a segment of a line (see below).");

		System.out.println();

		System.out.println("BONUS FEATURES");
		System.out.println("- Enter \"segment\" to find the Eliot Deviation Index of a segment of a line. You will need the stop orders from the full line.");
		System.out.println("- Enter \"reverse\" to reverse the order of the line.");

		System.out.println();

		System.out.println("Enjoy!");

		System.out.println();
		agency();
	}

	static void agency() {
		Scanner a = new Scanner(System.in);
		System.out.print("Agency: ");

		// check if valid agency
		ArrayList<String> agencies = Util.getAgencyIds();

		agencyChoice = a.nextLine();
		boolean actualAgency = false;
		for (String agency : agencies) {
			if (agency.equalsIgnoreCase(agencyChoice)) {
				actualAgency = true;
				edi();
			}
		}
		if (!actualAgency) {
			System.out.print("Invalid agency. ");
			agency();
		}
	}

	static void edi() {
		Scanner in = new Scanner(System.in);
		System.out.print("Enter line: ");
		String lineChoice = in.nextLine();

		while (!lineChoice.equals("-00")) {
			ArrayList<Stop> stops = new ArrayList<>();

			// create line
			int stopCount = 0;

			String lineName = "no data, yet";
			String actualName = "no data, yet";
			String branchName = "no data, yet";
			boolean official = false;
			boolean saved = false; // updates when asked to save
			boolean export = false; // export for submission

			Stop[] theLine;

			if (lineChoice.equalsIgnoreCase("set")) {
				System.out.print("Enter set: ");
				String setChoice = in.nextLine();

				agencyChoice = "sets/" + agencyChoice + "-" + setChoice;

				System.out.print("Enter line: ");
				lineChoice = in.nextLine();
			}

			if (lineChoice.equalsIgnoreCase("custom")) {
				// load in only existing stops
				stops.addAll(Util.getAgencyStops(agencyChoice));

				ArrayList<Stop> stops2 = new ArrayList<>(); // add to EDI list instead - this is in the files
				ArrayList<Stop> custom = new ArrayList<>(); // array for custom line - this is what shows at the end
				String customStop = "";

				System.out.print("Line Code: ");
				lineName = in.nextLine();

				System.out.print("Line Name: ");
				actualName = in.nextLine();

				System.out.print("Branch Name: ");
				branchName = in.nextLine();

				while (!customStop.equals("-0")) {
					System.out.print("Stop ID: ");
					customStop = in.nextLine();

					// custom stop location - will not add to agency listing, use addStop
					if (customStop.equals("-1")) {
						stopCount++;
						System.out.print("New Stop Name: ");
						String custName = in.nextLine();
						System.out.print("New Stop ID: ");
						String custID = in.nextLine();
						System.out.print("New Stop Latitude: ");
						double custLat = in.nextDouble();
						System.out.print("New Stop Longitude: ");
						double custLon = in.nextDouble();

						Stop addStop = new Stop(agencyChoice, custID, custName, custLat, custLon, lineName, stopCount);

						stops2.add(addStop);
						custom.add(addStop);

						in.nextLine(); // absorb enter
					}
					if (customStop.equals("-2")) {
						System.out.println(custom.get(custom.size() - 1).getName() + " (" + custom.get(custom.size() - 1).getId() + ") removed.");
						stops2.remove(stops2.size() - 1);
						custom.remove(custom.size() - 1);
						stopCount--;
					}

					// -3 - stop from another agency
					if (customStop.equals("-3")) {
						System.out.print("Enter agency: ");
						String extraAgency = in.nextLine();

						// load in stops from new agency
						ArrayList<Stop> stopsExtra = new ArrayList<>(); // not to confuse with stops
						try {
							Scanner extra = new Scanner(new File("stops/" + extraAgency + ".txt"));
							while (extra.hasNextLine()) {
								String data = extra.nextLine();
								String id = data.substring(0, data.indexOf(";"));
								data = data.substring(data.indexOf(";") + 1);
								String name = data.substring(0, data.indexOf(";"));
								data = data.substring(data.indexOf(";") + 1);
								double lat = Double.parseDouble(data.substring(0, data.indexOf(";")));
								data = data.substring(data.indexOf(";") + 1);
								double lon = Double.parseDouble(data);
								stopsExtra.add(new Stop(id, name, lat, lon));
							}
						} catch (Exception e) {
							System.out.println("Error. can't load stops");
						}

						System.out.print("Stop ID: "); // ask for new id
						customStop = in.nextLine();

						for (Stop stop : stopsExtra) {
							if (stop.getId().equalsIgnoreCase(customStop)) {
								stopCount++;
								stop.setId(extraAgency + " | " + stop.getId()); // extra due to formatting on display, need to know it's a different agency
								System.out.print("Added: " + stop.getName() + " "); // space for the stupidity
								Stop addStop = new Stop(agencyChoice, stop.getId(), stop.getName(), stop.getLat(), stop.getLon(), lineName, stopCount);
								stops2.add(addStop);
								custom.add(stop);
								break;
							}
						}
					}

					// add segment from existing route
					if (customStop.equals("segment")) {
						System.out.print("Enter line: ");
						lineChoice = in.nextLine();

						// segment reverse, almost the same damn thing as normal segment
						if (lineChoice.equals("reverse")) {
							System.out.print("Enter line: ");
							lineChoice = in.nextLine();
							System.out.print("Starting stop (low): ");
							int startStop = in.nextInt();
							System.out.print("Ending stop (high): ");
							int endStop = in.nextInt();

							// yes yes i know this is later on as well, but needs to be separate because of how i like the ordering on final file
							ArrayList<Stop> stops3 = Util.getAgencyRouteStops(agencyChoice); // stops3 - pulls from existing EDI file to add to segment

							Stop[] reverseLine = new Stop[(endStop - startStop) + 1];
							int reverseNum = reverseLine.length;
							// check if in part of line desired (change from normal segment)
							for (Stop stop : stops3) {
								if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= endStop && stop.getOrder() >= startStop) {
									reverseLine[reverseNum - 1] = new Stop(stop.getId(), stop.getName(), stop.getLat(), stop.getLon());
									reverseNum--;
								}
							}

							// add reverseLine into the main array, with correct stop ordering
							for (Stop stop : reverseLine) {
								stopCount++;
								stops2.add(new Stop(stop.getAgency(), stop.getId(), stop.getName(), stop.getLat(), stop.getLon(), lineName, stopCount));
								custom.add(stop);
							}
						}

						// normal segment (low to high)
						else {
							System.out.print("Starting stop: ");
							int startStop = in.nextInt();
							System.out.print("Ending stop: ");
							int endStop = in.nextInt();

							// yes yes i know this is later on as well, but needs to be separate because of how i like the ordering on final file
							ArrayList<Stop> stops3 = Util.getAgencyRouteStops(agencyChoice); // stops3 - pulls from existing EDI file to add to segment

							// check if in part of line desired
							for (Stop stop : stops3) {
								if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= endStop && stop.getOrder() >= startStop) {
									stopCount++;
									Stop addStop = new Stop(stop.getAgency(), stop.getId(), stop.getName(), stop.getLat(), stop.getLon(), lineName, stopCount);
									stops2.add(addStop);
									custom.add(new Stop(stop.getId(), stop.getName(), stop.getLat(), stop.getLon()));
								}
							}
						}
						in.nextLine(); // cancel out running rolling twice
					}

					// search for stop in listing
					else {
						for (Stop stop : stops) {
							if (stop.getId().equalsIgnoreCase(customStop)) {
								stopCount++;
								System.out.print("Added: " + stop.getName() + " "); // space for the stupidity
								Stop addStop = new Stop(stop.getAgency(), stop.getId(), stop.getName(), stop.getLat(), stop.getLon(), lineName, stopCount);
								stops2.add(addStop);
								custom.add(stop);
								break;
							}
						}
					}
					if (!customStop.equals("-0")) {
						rollingEDI(custom); // this is where shit gets stupid
					}
				}

				theLine = new Stop[custom.size()];
				for (int i = 0; i < custom.size(); i++) {
					theLine[i] = custom.get(i);
				}

				System.out.print("Save line? ");
				String save = in.nextLine();
				if (save.equalsIgnoreCase("yes")) {
					saved = true;
					// loads in EDI file to add route to list, different array
					stops2.addAll(Util.getAgencyRouteStops(agencyChoice));

					// adds list to -edi file, allows it to be read by calculator
					try {
						File newFile1 = new File("files/" + agencyChoice + "-edi.txt");
						FileWriter fileWriter1 = new FileWriter(newFile1);

						fileWriter1.write(stops2.get(0).getId() + ";" + stops2.get(0).getName() + ";" + stops2.get(0).getLat() + ";" + stops2.get(0).getLon() + ";" + stops2.get(0).getLine() + ";" + stops2.get(0).getOrder() + "\n");

						for (int i = 1; i < stops2.size(); i++) {
							fileWriter1.append(stops2.get(i).getId()).append(";").append(stops2.get(i).getName()).append(";").append(String.valueOf(stops2.get(i).getLat())).append(";").append(String.valueOf(stops2.get(i).getLon())).append(";").append(stops2.get(i).getLine()).append(";").append(String.valueOf(stops2.get(i).getOrder())).append("\n");
						}

						fileWriter1.close();

						System.out.println("Line added.");
					} catch (Exception e) {
						System.out.println("Error.");
					}
				}

				System.out.print("Export line? ");
				if (in.nextLine().equalsIgnoreCase("yes")) {
					export = true;
				}

				System.out.print("Official line? ");
				if (in.nextLine().equalsIgnoreCase("yes")) {
					official = true;
				}
			}

			// segments of routes
			else if (lineChoice.equalsIgnoreCase("segment")) {
				// loads in EDI file with existing routes only
				stops.addAll(Util.getAgencyRouteStops(agencyChoice));
				System.out.print("Enter line: ");
				lineChoice = in.nextLine();
				System.out.print("Starting stop: ");
				int startStop = in.nextInt();
				System.out.print("Ending stop: ");
				int endStop = in.nextInt();

				// check if in part of line desired
				for (Stop stop : stops) {
					if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= endStop && stop.getOrder() >= startStop) {
						stopCount++;
					}
				}

				if (stopCount == 0) {
					System.out.print("Invalid segment. ");
					edi();
					break;
				}

				theLine = new Stop[stopCount];
				for (Stop stop : stops) {
					if (stop.getLine().equalsIgnoreCase(lineChoice) && stop.getOrder() <= endStop && stop.getOrder() >= startStop) {
						theLine[stop.getOrder() - startStop] = stop;
					}
				}
			} else if (lineChoice.equalsIgnoreCase("reverse")) // reverses order
			{
				// loads in EDI file with existing routes only
				stops.addAll(Util.getAgencyRouteStops(agencyChoice));

				System.out.print("Enter line: ");
				lineChoice = in.nextLine();

				for (Stop stop : stops) {
					if (stop.getLine().equalsIgnoreCase(lineChoice)) {
						stopCount++;
					}
				}

				if (stopCount == 0) {
					System.out.print("Invalid line. ");
					edi();
					break;
				}

				// reversing using temp array
				Stop[] tempLine = new Stop[stopCount];
				theLine = new Stop[stopCount];
				for (Stop stop : stops) {
					if (stop.getLine().equalsIgnoreCase(lineChoice)) {
						tempLine[stop.getOrder() - 1] = stop;
					}
				}

				int remain = tempLine.length;
				for (int i = 0; i < tempLine.length; i++) {
					theLine[i] = tempLine[remain - 1];
					remain--;
				}
			}

			// find route in database
			else {
				// loads in EDI file with existing routes only
				stops.addAll(Util.getAgencyRouteStops(agencyChoice));

				// check if stop on line
				for (Stop stop : stops) {
					if (stop.getLine().equalsIgnoreCase(lineChoice)) {
						stopCount++;
					}
				}

				if (stopCount == 0) {
					System.out.print("Invalid line. ");
					edi();
					break;
				}

				theLine = new Stop[stopCount];
				for (Stop stop : stops) {
					if (stop.getLine().equalsIgnoreCase(lineChoice)) {
						theLine[stop.getOrder() - 1] = stop;
					}
				}
			}

			// rolling EDI for each stop added
			for (int i = 0; i < theLine.length; i++) {
				System.out.print("- " + theLine[i].getName() + " (" + theLine[i].getId() + ") ");
				// array for rolling EDI
				ArrayList<Stop> custom2 = new ArrayList<>(Arrays.asList(theLine).subList(0, i + 1));
				rollingEDI(custom2);
			}

			// haversine formula loop
			// spherical trig cause this is the globe
			double dist = 0;
			for (int i = 1; i < theLine.length; i++) {
				double lon1 = Math.toRadians(Math.abs(theLine[i - 1].getLon()));
				double lon2 = Math.toRadians(Math.abs(theLine[i].getLon()));
				double lat1 = Math.toRadians(Math.abs(theLine[i - 1].getLat()));
				double lat2 = Math.toRadians(Math.abs(theLine[i].getLat()));
				double dlon = lon2 - lon1;
				double dlat = lat2 - lat1;
				double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
				double c = 2 * Math.asin(Math.sqrt(a));
				double r = 3963;

				dist += c * r;
			}

			// full line haversine
			double firstLon = Math.toRadians(theLine[0].getLon());
			double lastLon = Math.toRadians(theLine[theLine.length - 1].getLon()); // 11 is temp
			double firstLat = Math.toRadians(theLine[0].getLat());
			double lastLat = Math.toRadians(theLine[theLine.length - 1].getLat()); // 11 is temp
			double difflon = 0; // placeholder
			double difflat = 0; // placeholder

			// longitude math
			if (firstLon >= 0 && lastLon >= 0) {
				difflon = lastLon - firstLon;
			} else if (firstLon <= 0 && lastLon <= 0) {
				lastLon = Math.abs(lastLon);
				firstLon = Math.abs(firstLon);
				difflon = lastLon - firstLon;
			} else if (firstLon <= 0 && lastLon >= 0) {
				difflon = Math.abs(firstLon - lastLon);
			} else if (firstLon >= 0 && lastLon <= 0) {
				difflon = Math.abs(lastLon - firstLon);
			}

			// latitude math
			if (firstLat >= 0 && lastLat >= 0) {
				difflat = lastLat - firstLat;
			} else if (firstLat <= 0 && lastLat <= 0) {
				lastLat = Math.abs(lastLat);
				firstLat = Math.abs(firstLat);
				difflat = lastLat - firstLat;
			} else if (firstLat <= 0 && lastLat >= 0) {
				difflat = Math.abs(firstLat - lastLat);
			} else if (firstLat >= 0 && lastLat <= 0) {
				difflat = Math.abs(lastLat - firstLat);
			}

			double a1 = Math.pow(Math.sin(difflat / 2), 2) + Math.cos(firstLat) * Math.cos(lastLat) * Math.pow(Math.sin(difflon / 2), 2);
			double c1 = 2 * Math.asin(Math.sqrt(a1));
			double r1 = 3963;
			double lineDist = c1 * r1;

			// calculate the edi
			double edi = dist / lineDist;
			edi = Math.round(edi * 100.0) / 100.0;

			if (edi < 1) // case for the 0.9x
			{
				edi = 1.0;
			}

			// assorted calcs
			dist = Math.round(dist * 100.0) / 100.0;
			double avgStop = dist / (theLine.length - 1);
			avgStop = Math.round(avgStop * 100.0) / 100.0;

			// display rounded info
			System.out.println("Distance: " + dist + " miles");
			System.out.println("Eliot Deviation Index: " + edi);
			System.out.println("Average Stop Spacing: " + avgStop + " miles");

			// this is when things get added to site listing
			if (saved) {
				ArrayList<String> routeCode = new ArrayList<>();
				ArrayList<String> routeDist = new ArrayList<>();
				ArrayList<String> routeEdi = new ArrayList<>();
				ArrayList<String> routeName = new ArrayList<>();
				ArrayList<String> routeBranch = new ArrayList<>();

				// add to .txt file, gets converted in routeList
				try {
					Scanner s = new Scanner(new File("edis/" + agencyChoice + ".txt"));
					while (s.hasNextLine()) {
						String data = s.nextLine();
						String code = data.substring(0, data.indexOf(";"));
						routeCode.add(code);
						data = data.substring(data.indexOf(";") + 1);
						String dist2 = data.substring(0, data.indexOf(";"));
						routeDist.add(dist2);
						data = data.substring(data.indexOf(";") + 1);
						String ediA = data.substring(0, data.indexOf(";"));
						routeEdi.add(ediA);
						data = data.substring(data.indexOf(";") + 1);
						String name = data.substring(0, data.indexOf(";"));
						routeName.add(name);
						data = data.substring(data.indexOf(";") + 1);
						String branch = data;
						routeBranch.add(branch);
					}
				} catch (Exception e) {
					System.out.println("Error, no EDI list file for agency " + agencyChoice + "."); // expected error if first route
				}

				// add to new list if first
				if (routeCode.isEmpty()) {
					if (official) {
						routeCode.add(lineName);
					} else {
						routeCode.add("!" + lineName);
					}
					routeDist.add(dist + "");
					routeEdi.add(edi + "");
					routeName.add(actualName);
					routeBranch.add(branchName);
				}
				// select position on list if list exists
				else {
					for (int z = 0; z < routeCode.size(); z++) {
						System.out.println((z + 1) + ". " + routeCode.get(z) + " (" + routeDist.get(z) + " mi., " + routeEdi.get(z) + ")");
					}

					Scanner list = new Scanner(System.in);
					System.out.print("Enter listing before: ");
					int listing = list.nextInt();

					if (official) {
						routeCode.add(listing, lineName);
					} else {
						routeCode.add(listing, "!" + lineName);
					}
					routeDist.add(listing, dist + "");
					routeEdi.add(listing, edi + "");
					routeName.add(listing, actualName);
					routeBranch.add(listing, branchName);
				}

				// add EDI to list.
				try {
					File newFile1 = new File("edis/" + agencyChoice + ".txt");
					FileWriter fileWriter1 = new FileWriter(newFile1);

					fileWriter1.write(routeCode.get(0) + ";" + routeDist.get(0) + ";" + routeEdi.get(0) + ";" + routeName.get(0) + ";" + routeBranch.get(0) + "\n");

					for (int b = 1; b < routeCode.size(); b++) {
						fileWriter1.append(routeCode.get(b)).append(";").append(routeDist.get(b)).append(";").append(routeEdi.get(b)).append(";").append(routeName.get(b)).append(";").append(routeBranch.get(b)).append("\n");
					}

					fileWriter1.close();

					System.out.println("Line added.");
				} catch (Exception e) {
					System.out.println("Error.");
				}
			}

			if (export) {
				System.out.println("Export File (copy below this line):");
				System.out.println("Agency: " + agencyChoice);
				System.out.println("Line: " + lineName);

				for (Stop stop : theLine) {
					System.out.println(stop.getId());
				}

				System.out.println("-0");
			}

			System.out.print("Enter next line (-00 to exit): ");
			lineChoice = in.nextLine();
		}
	}

	// rolling calculator for custom lines, this is a stupid idea but it's 1 am and i feel like adding it
	static void rollingEDI(ArrayList<Stop> custom) {
		Stop[] theLine = new Stop[custom.size()];
		for (int i = 0; i < custom.size(); i++) {
			theLine[i] = custom.get(i);
		}

		// haversine formula loop
		double dist = 0;
		for (int i = 1; i < theLine.length; i++) {
			double lon1 = Math.toRadians(Math.abs(theLine[i - 1].getLon()));
			double lon2 = Math.toRadians(Math.abs(theLine[i].getLon()));
			double lat1 = Math.toRadians(Math.abs(theLine[i - 1].getLat()));
			double lat2 = Math.toRadians(Math.abs(theLine[i].getLat()));
			double dlon = lon2 - lon1;
			double dlat = lat2 - lat1;
			double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
			double c = 2 * Math.asin(Math.sqrt(a));
			double r = 3963;

			dist += c * r;
		}

		// full line haversine
		double firstLon = Math.toRadians(theLine[0].getLon());
		double lastLon = Math.toRadians(theLine[theLine.length - 1].getLon());
		double firstLat = Math.toRadians(theLine[0].getLat());
		double lastLat = Math.toRadians(theLine[theLine.length - 1].getLat());
		double difflon = 0; // placeholder
		double difflat = 0; // placeholder

		// longitude math
		if (firstLon >= 0 && lastLon >= 0) {
			difflon = lastLon - firstLon;
		} else if (firstLon <= 0 && lastLon <= 0) {
			lastLon = Math.abs(lastLon);
			firstLon = Math.abs(firstLon);
			difflon = lastLon - firstLon;
		} else if (firstLon <= 0 && lastLon >= 0) {
			difflon = Math.abs(firstLon - lastLon);
		} else if (firstLon >= 0 && lastLon <= 0) {
			difflon = Math.abs(lastLon - firstLon);
		}

		// latitude math
		if (firstLat >= 0 && lastLat >= 0) {
			difflat = lastLat - firstLat;
		} else if (firstLat <= 0 && lastLat <= 0) {
			lastLat = Math.abs(lastLat);
			firstLat = Math.abs(firstLat);
			difflat = lastLat - firstLat;
		} else if (firstLat <= 0 && lastLat >= 0) {
			difflat = Math.abs(firstLat - lastLat);
		} else if (firstLat >= 0 && lastLat <= 0) {
			difflat = Math.abs(lastLat - firstLat);
		}

		double a1 = Math.pow(Math.sin(difflat / 2), 2) + Math.cos(firstLat) * Math.cos(lastLat) * Math.pow(Math.sin(difflon / 2), 2);
		double c1 = 2 * Math.asin(Math.sqrt(a1));
		double r1 = 3963;
		double lineDist = c1 * r1;

		// calculate the edi
		double edi = dist / lineDist;
		edi = Math.round(edi * 100.0) / 100.0;

		if (edi < 1) // case for the 0.9x
		{
			edi = 1.0;
		}

		dist = Math.round(dist * 100.0) / 100.0;
		System.out.print("(" + theLine.length + ", " + dist + ", ");
		System.out.println(edi + ")");
	}

	// exact clone of rollingEdi(), but returns just the EDI value
	static double calcIndex(Stop[] theLine) {
		// haversine formula loop
		double dist = 0;
		for (int i = 1; i < theLine.length; i++) {
			double lon1 = Math.toRadians(Math.abs(theLine[i - 1].getLon()));
			double lon2 = Math.toRadians(Math.abs(theLine[i].getLon()));
			double lat1 = Math.toRadians(Math.abs(theLine[i - 1].getLat()));
			double lat2 = Math.toRadians(Math.abs(theLine[i].getLat()));
			double dlon = lon2 - lon1;
			double dlat = lat2 - lat1;
			double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
			double c = 2 * Math.asin(Math.sqrt(a));
			double r = 3963;

			dist += c * r;
		}

		// full line haversine
		double firstLon = Math.toRadians(theLine[0].getLon());
		double lastLon = Math.toRadians(theLine[theLine.length - 1].getLon());
		double firstLat = Math.toRadians(theLine[0].getLat());
		double lastLat = Math.toRadians(theLine[theLine.length - 1].getLat());
		double difflon = 0; // placeholder
		double difflat = 0; // placeholder

		// longitude math
		if (firstLon >= 0 && lastLon >= 0) {
			difflon = lastLon - firstLon;
		} else if (firstLon <= 0 && lastLon <= 0) {
			lastLon = Math.abs(lastLon);
			firstLon = Math.abs(firstLon);
			difflon = lastLon - firstLon;
		} else if (firstLon <= 0 && lastLon >= 0) {
			difflon = Math.abs(firstLon - lastLon);
		} else if (firstLon >= 0 && lastLon <= 0) {
			difflon = Math.abs(lastLon - firstLon);
		}

		// latitude math
		if (firstLat >= 0 && lastLat >= 0) {
			difflat = lastLat - firstLat;
		} else if (firstLat <= 0 && lastLat <= 0) {
			lastLat = Math.abs(lastLat);
			firstLat = Math.abs(firstLat);
			difflat = lastLat - firstLat;
		} else if (firstLat <= 0 && lastLat >= 0) {
			difflat = Math.abs(firstLat - lastLat);
		} else if (firstLat >= 0 && lastLat <= 0) {
			difflat = Math.abs(lastLat - firstLat);
		}

		double a1 = Math.pow(Math.sin(difflat / 2), 2) + Math.cos(firstLat) * Math.cos(lastLat) * Math.pow(Math.sin(difflon / 2), 2);
		double c1 = 2 * Math.asin(Math.sqrt(a1));
		double r1 = 3963;
		double lineDist = c1 * r1;

		// calculate the edi
		double edi = dist / lineDist;
		edi = Math.round(edi * 100.0) / 100.0;

		if (edi < 1) // case for the 0.9x
		{
			edi = 1.0;
		}

		return edi; // returns to whatever called it (in the original case, it's the Segment.java)
	}

	// yet another clone, this time for import (things need to be slightly different each time, yay)
	static void saveIndex(Stop[] theLine, String lineName, String actualName, String branchName, String agencyChoice) {
		// haversine formula loop
		double dist = 0;
		for (int i = 1; i < theLine.length; i++) {
			double lon1 = Math.toRadians(Math.abs(theLine[i - 1].getLon()));
			double lon2 = Math.toRadians(Math.abs(theLine[i].getLon()));
			double lat1 = Math.toRadians(Math.abs(theLine[i - 1].getLat()));
			double lat2 = Math.toRadians(Math.abs(theLine[i].getLat()));
			double dlon = lon2 - lon1;
			double dlat = lat2 - lat1;
			double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
			double c = 2 * Math.asin(Math.sqrt(a));
			double r = 3963;

			dist += c * r;
		}

		// full line haversine
		double firstLon = Math.toRadians(theLine[0].getLon());
		double lastLon = Math.toRadians(theLine[theLine.length - 1].getLon());
		double firstLat = Math.toRadians(theLine[0].getLat());
		double lastLat = Math.toRadians(theLine[theLine.length - 1].getLat());
		double difflon = 0; // placeholder
		double difflat = 0; // placeholder

		// longitude math
		if (firstLon >= 0 && lastLon >= 0) {
			difflon = lastLon - firstLon;
		} else if (firstLon <= 0 && lastLon <= 0) {
			lastLon = Math.abs(lastLon);
			firstLon = Math.abs(firstLon);
			difflon = lastLon - firstLon;
		} else if (firstLon <= 0 && lastLon >= 0) {
			difflon = Math.abs(firstLon - lastLon);
		} else if (firstLon >= 0 && lastLon <= 0) {
			difflon = Math.abs(lastLon - firstLon);
		}

		// latitude math
		if (firstLat >= 0 && lastLat >= 0) {
			difflat = lastLat - firstLat;
		} else if (firstLat <= 0 && lastLat <= 0) {
			lastLat = Math.abs(lastLat);
			firstLat = Math.abs(firstLat);
			difflat = lastLat - firstLat;
		} else if (firstLat <= 0 && lastLat >= 0) {
			difflat = Math.abs(firstLat - lastLat);
		} else if (firstLat >= 0 && lastLat <= 0) {
			difflat = Math.abs(lastLat - firstLat);
		}

		double a1 = Math.pow(Math.sin(difflat / 2), 2) + Math.cos(firstLat) * Math.cos(lastLat) * Math.pow(Math.sin(difflon / 2), 2);
		double c1 = 2 * Math.asin(Math.sqrt(a1));
		double r1 = 3963;
		double lineDist = c1 * r1;

		// calculate the edi
		double edi = dist / lineDist;
		edi = Math.round(edi * 100.0) / 100.0;

		if (edi < 1) // case for the 0.9x
		{
			edi = 1.0;
		}

		dist = Math.round(dist * 100.0) / 100.0;

		ArrayList<String> routeCode = new ArrayList<>();
		ArrayList<String> routeDist = new ArrayList<>();
		ArrayList<String> routeEdi = new ArrayList<>();
		ArrayList<String> routeName = new ArrayList<>();
		ArrayList<String> routeBranch = new ArrayList<>();

		// add to .txt file, gets converted in routeList
		try {
			Scanner s = new Scanner(new File("edis/" + agencyChoice + ".txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				String code = data.substring(0, data.indexOf(";"));
				routeCode.add(code);
				data = data.substring(data.indexOf(";") + 1);
				String dist2 = data.substring(0, data.indexOf(";"));
				routeDist.add(dist2);
				data = data.substring(data.indexOf(";") + 1);
				String ediA = data.substring(0, data.indexOf(";"));
				routeEdi.add(ediA);
				data = data.substring(data.indexOf(";") + 1);
				String name = data.substring(0, data.indexOf(";"));
				routeName.add(name);
				data = data.substring(data.indexOf(";") + 1);
				String branch = data;
				routeBranch.add(branch);
			}
		} catch (Exception e) {
			System.out.println("Error, no EDI list file for agency " + agencyChoice + "."); // expected error if first route
		}

		routeCode.add(lineName);
		routeDist.add(dist + "");
		routeEdi.add(edi + "");
		routeName.add(actualName);
		routeBranch.add(branchName);

		// add EDI to list.
		try {
			File newFile1 = new File("edis/" + agencyChoice + ".txt");
			FileWriter fileWriter1 = new FileWriter(newFile1);

			fileWriter1.write(routeCode.get(0) + ";" + routeDist.get(0) + ";" + routeEdi.get(0) + ";" + routeName.get(0) + ";" + routeBranch.get(0) + "\n");

			for (int b = 1; b < routeCode.size(); b++) {
				fileWriter1.append(routeCode.get(b)).append(";").append(routeDist.get(b)).append(";").append(routeEdi.get(b)).append(";").append(routeName.get(b)).append(";").append(routeBranch.get(b)).append("\n");
			}

			fileWriter1.close();

			// print line just added
			System.out.println(actualName + " (" + branchName + ")" + " (" + lineName + ", " + dist + " mi., " + edi + ")");
		} catch (Exception e) {
			System.out.println("Error.");
		}

		// loads in EDI file to add route to list, different array
		ArrayList<Stop> stops2 = new ArrayList<>();
		for (int i = 0; i < theLine.length; i++) {
			stops2.add(theLine[i]);
			stops2.get(stops2.size() - 1).setLine(lineName);
			stops2.get(stops2.size() - 1).setOrder(i + 1);
		}
		stops2.addAll(Util.getAgencyRouteStops(agencyChoice));

		// adds list to -edi file, allows it to be read by calculator
		try {
			File newFile1 = new File("files/" + agencyChoice + "-edi.txt");
			FileWriter fileWriter1 = new FileWriter(newFile1);

			fileWriter1.write(stops2.get(0).getId() + ";" + stops2.get(0).getName() + ";" + stops2.get(0).getLat() + ";" + stops2.get(0).getLon() + ";" + stops2.get(0).getLine() + ";" + stops2.get(0).getOrder() + "\n");

			for (int i = 1; i < stops2.size(); i++) {
				fileWriter1.append(stops2.get(i).getId()).append(";").append(stops2.get(i).getName()).append(";").append(String.valueOf(stops2.get(i).getLat())).append(";").append(String.valueOf(stops2.get(i).getLon())).append(";").append(stops2.get(i).getLine()).append(";").append(String.valueOf(stops2.get(i).getOrder())).append("\n");
			}

			fileWriter1.close();
		} catch (Exception e) {
			System.out.println("Error.");
		}
	}
}