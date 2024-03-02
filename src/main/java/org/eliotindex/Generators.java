package org.eliotindex;

import de.micromata.opengis.kml.v_2_2_0.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Generators {
	@SuppressWarnings("SpellCheckingInspection")
	private static Document getAgencyMap(String agency) {
		ArrayList<Stop> stops = Util.getAgencyRouteStops(agency);
		if (stops.isEmpty()) return null;

		ArrayList<Route> routes = Util.getAgencyEdis(agency);
		Document document = new Document().withName(agency + " routes");

		LinkedHashMap<String, String> styles = new LinkedHashMap<>();
		styles.put("1.0", "ff10c283");
		styles.put("1.5", "ff195c03");
		styles.put("2.0", "ffa0ad10");
		styles.put("2.5", "ffad4902");
		styles.put("3.0", "ffbf1d7e");
		styles.put("3.5", "ff6e0cb0");
		styles.put("4.0", "ff190177");
		styles.put("10.0", "ff000000");

		styles.forEach((id, color) -> document.createAndAddStyle()
				.withId(id)
				.createAndSetLineStyle()
				.withColor(color)
				.withWidth(4.0));

		for (Route route : routes) {
			double style = 10.0;

			if (route.getEdi() <= 10.0) {
				style = Math.min(Math.floor(route.getEdi() * 2) / 2, 4.0);
			}

			String description = "Branch: " + route.getBranch() + "<br/>" +
					"Agency: " + agency + "<br/>" +
					"EDI: " + route.getEdi();

			List<Coordinate> coordinates = new ArrayList<>();
			for (Stop stop : stops) {
				if (stop.getLine().equals(route.getLineCode())) {
					coordinates.add(new Coordinate(stop.getLon(), stop.getLat()));
				}
			}
			Placemark placemark = new Placemark()
					.withName(route.getLineName())
					.withStyleUrl("#" + style)
					.withDescription(description);
			placemark.createAndSetLineString().withCoordinates(coordinates);


			document.addToFeature(placemark);
		}

		return document;
	}

	private static void writeKmlFile(Feature feature, String filePath) {
		try {
			Kml kml = new Kml().withFeature(feature);
			kml.marshal(new File(filePath));

			Path path = Paths.get(filePath);
			String content = new String(Files.readAllBytes(path));
			content = content.replaceAll("kml:", "");
			Files.write(path, content.getBytes());
		} catch (IOException e) {
			System.out.println("Error writing map.");
		}
	}

	/**
	 * Creates individual agency maps
	 */
	static void agencyMap() {
		ArrayList<String> agencies = Util.getAgencyIds();

		for (String agency : agencies) {
			Document document = getAgencyMap(agency);
			if (document == null) continue;

			writeKmlFile(document, "public/maps/map-" + agency + ".kml");
		}
	}

	/**
	 * Generates maps/lines/map-all.kml
	 */
	static void fullMap() {
		ArrayList<String> agencies = Util.getAgencyIds();
		Folder folder = new Folder().withName("Eliot Deviation Index Routes");

		for (String agency : agencies) {
			Document document = getAgencyMap(agency);
			if (document == null) continue;
			folder.addToFeature(document);
		}

		writeKmlFile(folder, "public/maps/map-all.kml");
	}

	/**
	 * Generates routes.html
	 */
	static void routeList() {
		ArrayList<String> agencies = Util.getAgencyIds();
		ArrayList<String> fullAgencies = Util.getAgencyNames();

		// creates page
		try {
			File newFile1 = new File("public/routes.html");
			FileWriter fileWriter1 = new FileWriter(newFile1);

			fileWriter1.write("<title>Route Listing - Eliot Deviation Index</title> \n");
			fileWriter1.append("<link rel=stylesheet href=style.css> \n");
			fileWriter1.append("<ul><li><a href=index.html>Home</a></li>");
			fileWriter1.append("<li><a href=stops.html>Stop Listing</a></li> \n");
			fileWriter1.append("<li><a href=routes.html class=active>Route Listing</a></li> \n");
			fileWriter1.append("<li><a href=detailed.html>Route Detail</a></li> \n");
			fileWriter1.append("<li><a href=stats.html>Statistics</a></li> \n");
			fileWriter1.append("<li><a href=calculator.html>Calculator</a></li></ul> \n");
			fileWriter1.append("<h1>Route Listing</h1> \n");

			// listing with links at top
			for (int i = 0; i < agencies.size(); i++) {
				fileWriter1.append("<ul class=bullet style=background-color:#1f1f1f><a href=#").append(agencies.get(i)).append(">").append(fullAgencies.get(i)).append("</a> (").append(agencies.get(i)).append(")</ul> \n");
			}

			int routeCount = 0;

			// loop to list routes
			for (int i = 0; i < agencies.size(); i++) {
				fileWriter1.append("<h3 id=").append(agencies.get(i)).append(">").append(fullAgencies.get(i)).append(" (").append(agencies.get(i)).append(")</h3> \n");

				ArrayList<String> routeCode = new ArrayList<>();
				ArrayList<String> routeDist = new ArrayList<>();
				ArrayList<String> routeEdi = new ArrayList<>();
				ArrayList<String> routeOfficial = new ArrayList<>();
				int agencyCount = 0;
				// takes routes from /edi folder
				try {
					Scanner s = new Scanner(new File("edis/" + agencies.get(i) + ".txt"));
					while (s.hasNextLine()) {
						String data = s.nextLine();
						if (data.charAt(0) != '*') // * - unverified
						{
							if (data.charAt(0) == '!') {
								data = data.substring(1);
								routeOfficial.add("n");
							} else {
								routeOfficial.add("y");
							}
							String code = data.substring(0, data.indexOf(";"));
							routeCode.add(code);
							data = data.substring(data.indexOf(";") + 1);
							String dist = data.substring(0, data.indexOf(";"));
							routeDist.add(dist);
							data = data.substring(data.indexOf(";") + 1);
							String edi = data.substring(0, data.indexOf(";"));
							routeEdi.add(edi);
							routeCount++;
							agencyCount++;
						}
					}
				} catch (FileNotFoundException e) {
					// System.out.println("Error, no EDI file (" + agencies.get(i) + ")"); // expected error if no routes in database.
				}

				fileWriter1.append("<table><tr><th>Route Code</th><th>Line Length (Miles)</th><th>Line Length (Kilometers)</th><th>Eliot Deviation Index</th></tr> \n");

				// route table created
				for (int j = 0; j < routeCode.size(); j++) {
					fileWriter1.append("<tr><td style=color:")
							.append(routeOfficial.get(j).equals("y") ? "#1aab2d" : "#0097a7")
							.append(">")
							.append(routeCode.get(j))
							.append("</td><td>")
							.append(routeDist.get(j))
							.append(" mi.</td><td>")
							.append(Double.toString(Math.round(Double.parseDouble(routeDist.get(j)) * 1.60934 * 100.0) / 100.0))
							.append(" km.</td><td>")
							.append(routeEdi.get(j))
							.append("</td></tr> \n");
				}

				fileWriter1.append("</table> \n");
				System.out.println("Agency Routes (" + agencies.get(i) + "): " + agencyCount);
				int currentSize = agencyCount;

				// check for sets
				try {
					// load in the sets
					ArrayList<String> agencySets = new ArrayList<>();
					Scanner t = new Scanner(new File("sets/" + agencies.get(i) + ".txt"));
					while (t.hasNextLine()) {
						String data = t.nextLine();
						agencySets.add(data);
					}

					// load in from the set
					// loop through all sets
					for (String agencySet : agencySets) {
						Scanner s = new Scanner(new File("edis/sets/" + agencies.get(i) + "-" + agencySet + ".txt"));
						while (s.hasNextLine()) {
							String data = s.nextLine();
							// i'm gonna list them all blue (unofficial b/c post-rollover)
							String code = data.substring(0, data.indexOf(";"));
							routeCode.add(code);
							data = data.substring(data.indexOf(";") + 1);
							String dist = data.substring(0, data.indexOf(";"));
							routeDist.add(dist);
							data = data.substring(data.indexOf(";") + 1);
							String edi = data;
							routeEdi.add(edi);
							routeCount++;
							agencyCount++;
						}

						fileWriter1.append("<p><b>").append(agencySet).append(" Set</b></p>\n");
						fileWriter1.append("<table><tr><th>Route Code</th><th>Line Length (Miles)</th><th>Line Length (Kilometers)</th><th>Eliot Deviation Index</th></tr> \n");

						for (int k = currentSize; k < routeCode.size(); k++) {
							fileWriter1.append("<tr><td style=color:#0097a7>")
									.append(routeCode.get(k))
									.append("</td><td>")
									.append(routeDist.get(k))
									.append(" mi.</td><td>")
									.append(Double.toString(Math.round(Double.parseDouble(routeDist.get(k)) * 1.60934 * 100.0) / 100.0))
									.append(" km.</td><td>")
									.append(routeEdi.get(k))
									.append("</td></tr> \n");
						}

						fileWriter1.append("</table> \n");
						currentSize = agencyCount; // set things for the next set if applicable
					}
				} catch (Exception e) {
					// just don't do anything then
				}
			}

			fileWriter1.append("<p><b>Route Count: </b> ").append(String.valueOf(routeCount)).append("</p>");
			fileWriter1.close();
			System.out.println("Route Count: " + routeCount);
		} catch (IOException e) {
			System.out.println("Error, can't save routes.");
		}
	}

	/**
	 * Creates stats page
	 * Stats command line in Stats.java, no page generation there
	 * Basically the same code though
	 * EDI miles will probably be ignored
	 */
	static void statsList() {
		// start with the global
		ArrayList<String> agencies = new ArrayList<>();
		ArrayList<String> fullAgencies = new ArrayList<>();
		try {
			Scanner s = new Scanner(new File("agencies.txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				String code = data.substring(0, data.indexOf(";"));
				agencies.add(code);
				data = data.substring(data.indexOf(";") + 1);
				fullAgencies.add(data);
			}
		} catch (Exception e) {
			System.out.println("Error - no agencies.");
		}

		try {
			File newFile1 = new File("public/stats.html");
			FileWriter fileWriter1 = new FileWriter(newFile1);

			fileWriter1.write("<title>Statistics - Eliot Deviation Index</title> \n");
			fileWriter1.append("<link rel=stylesheet href=style.css> \n");
			fileWriter1.append("<ul><li><a href=index.html>Home</a></li>");
			fileWriter1.append("<li><a href=stops.html>Stop Listing</a></li> \n");
			fileWriter1.append("<li><a href=routes.html>Route Listing</a></li> \n");
			fileWriter1.append("<li><a href=detailed.html>Route Detail</a></li> \n");
			fileWriter1.append("<li><a href=stats.html class=active>Statistics</a></li> \n");
			fileWriter1.append("<li><a href=calculator.html>Calculator</a></li></ul> \n");
			fileWriter1.append("<h1>Statistics</h1> \n");

			// listing with links at top
			fileWriter1.append("<ul class=bullet style=background-color:#1f1f1f><a href=#global>Entire Database</a></ul> \n");
			for (int i = 0; i < agencies.size(); i++) {
				fileWriter1.append("<ul class=bullet style=background-color:#1f1f1f><a href=#").append(agencies.get(i)).append(">").append(fullAgencies.get(i)).append("</a> (").append(agencies.get(i)).append(")</ul> \n");
			}

			fileWriter1.append("<h3 id=global>Entire Database</h3> \n");

			ArrayList<Double> lengths = new ArrayList<>(); // stores all lengths
			ArrayList<Double> edis = new ArrayList<>();
			double totals = 0.00; // total EDI miles in agency

			for (String agency : agencies) {
				try {
					Scanner s = new Scanner(new File("edis/" + agency + ".txt"));
					while (s.hasNextLine()) {
						String data = s.nextLine();
						data = data.substring(data.indexOf(";") + 1);
						double miles = Double.parseDouble(data.substring(0, data.indexOf(";")));
						data = data.substring(data.indexOf(";") + 1);
						double edi = Double.parseDouble(data.substring(0, data.indexOf(";")));

						lengths.add(miles);
						edis.add(edi);
						totals += miles;
					}
				} catch (Exception e) {
					continue; // skip because agency not in database
				}

				// check for sets - yes, sets are being counted with global stats list
				try {
					// load in the sets
					ArrayList<String> agencySets = new ArrayList<>();
					Scanner t = new Scanner(new File("sets/" + agency + ".txt"));
					while (t.hasNextLine()) {
						String data = t.nextLine();
						agencySets.add(data);
					}

					// load in from the set
					// loop through all sets
					for (String agencySet : agencySets) {
						Scanner s = new Scanner(new File("edis/sets/" + agency + "-" + agencySet + ".txt"));
						while (s.hasNextLine()) {
							String data = s.nextLine();
							data = data.substring(data.indexOf(";") + 1);
							double miles = Double.parseDouble(data.substring(0, data.indexOf(";")));
							data = data.substring(data.indexOf(";") + 1);
							double edi = Double.parseDouble(data);

							lengths.add(miles);
							edis.add(edi);
							totals += miles;
						}
					}
				} catch (Exception e) {
					// skip, agency has no sets
				}
			}

			// does all the math for the global
			Collections.sort(lengths);
			Collections.sort(edis);

			double meanEdi;
			double medianEdi;
			double meanLength;
			double medianLength;

			// mean EDI
			double totalEdi = 0.0;
			for (Double aDouble : edis) {
				totalEdi = totalEdi + aDouble;
			}
			meanEdi = totalEdi / edis.size();
			meanEdi = Math.round(meanEdi * 100.0) / 100.0;

			// mean length
			double totalLength = 0.0;
			for (Double length : lengths) {
				totalLength = totalLength + length;
			}
			meanLength = totalLength / lengths.size();
			meanLength = Math.round(meanLength * 100.0) / 100.0;

			// median EDI
			if (edis.size() % 2 == 1) // odd amount of EDIs
			{
				if (edis.size() == 1) {
					medianEdi = edis.get(0);
				} else {
					medianEdi = edis.get(edis.size() / 2);
				}
			} else // even amount of EDIs
			{
				if (edis.size() == 2) {
					medianEdi = (edis.get(0) + edis.get(1)) / 2.0;
				} else {
					medianEdi = (edis.get((edis.size() / 2) - 1) + edis.get(edis.size() / 2)) / 2.0;
				}
				medianEdi = Math.round(medianEdi * 100.0) / 100.0;
			}

			// median length
			if (lengths.size() % 2 == 1) // odd amount of lengths
			{
				if (lengths.size() == 1) {
					medianLength = lengths.get(0);
				} else {
					medianLength = lengths.get(lengths.size() / 2);
				}
			} else // even amount of lengths
			{
				if (lengths.size() == 2) {
					medianLength = (lengths.get(0) + lengths.get(1)) / 2.0;
				} else {
					medianLength = (lengths.get((lengths.size() / 2) - 1) + lengths.get(lengths.size() / 2)) / 2.0;
				}
				medianLength = Math.round(medianLength * 100.0) / 100.0;
			}

			// round total miles
			totals = Math.round(totals * 100.0) / 100.0;

			fileWriter1.append("<p><b>Total Routes: </b>").append(String.valueOf(lengths.size())).append("<br>\n");
			fileWriter1.append("<b>Mean EDI: </b>").append(String.valueOf(meanEdi)).append("<br>\n");
			fileWriter1.append("<b>Median EDI: </b>").append(String.valueOf(medianEdi)).append("<br>\n");
			fileWriter1.append("<b>Mean Length: </b>").append(String.valueOf(meanLength)).append(" mi.<br>\n");
			fileWriter1.append("<b>Median Length: </b>").append(String.valueOf(medianLength)).append(" mi.</p>\n");

			lengths.clear();
			edis.clear();

			// loop through all the agencies
			for (int a = 0; a < agencies.size(); a++) {
				fileWriter1.append("<h3 id=").append(agencies.get(a)).append(">").append(fullAgencies.get(a)).append(" (").append(agencies.get(a)).append(")</h3> \n");

				try {
					Scanner s = new Scanner(new File("edis/" + agencies.get(a) + ".txt"));
					while (s.hasNextLine()) {
						String data = s.nextLine();
						data = data.substring(data.indexOf(";") + 1);
						double miles = Double.parseDouble(data.substring(0, data.indexOf(";")));
						data = data.substring(data.indexOf(";") + 1);
						double edi = Double.parseDouble(data.substring(0, data.indexOf(";")));

						lengths.add(miles);
						edis.add(edi);
						totals += miles;
					}
				} catch (Exception e) {
					continue; // skip because agency not in database
				}

				Collections.sort(lengths);
				Collections.sort(edis);

				// mean EDI
				totalEdi = 0.0;
				for (Double aDouble : edis) {
					totalEdi = totalEdi + aDouble;
				}
				meanEdi = totalEdi / edis.size();
				meanEdi = Math.round(meanEdi * 100.0) / 100.0;

				// mean length
				totalLength = 0.0;
				for (Double length : lengths) {
					totalLength = totalLength + length;
				}
				meanLength = totalLength / lengths.size();
				meanLength = Math.round(meanLength * 100.0) / 100.0;

				// median EDI
				if (edis.size() % 2 == 1) // odd amount of EDIs
				{
					if (edis.size() == 1) {
						medianEdi = edis.get(0);
					} else {
						medianEdi = edis.get(edis.size() / 2);
					}
				} else // even amount of EDIs
				{
					if (edis.size() == 2) {
						medianEdi = (edis.get(0) + edis.get(1)) / 2.0;
					} else {
						medianEdi = (edis.get((edis.size() / 2) - 1) + edis.get(edis.size() / 2)) / 2.0;
					}
					medianEdi = Math.round(medianEdi * 100.0) / 100.0;
				}

				// median length
				if (lengths.size() % 2 == 1) // odd amount of lengths
				{
					if (lengths.size() == 1) {
						medianLength = lengths.get(0);
					} else {
						medianLength = lengths.get(lengths.size() / 2);
					}
				} else // even amount of lengths
				{
					if (lengths.size() == 2) {
						medianLength = (lengths.get(0) + lengths.get(1)) / 2.0;
					} else {
						medianLength = (lengths.get((lengths.size() / 2) - 1) + lengths.get(lengths.size() / 2)) / 2.0;
					}
					medianLength = Math.round(medianLength * 100.0) / 100.0;
				}

				// round total miles
				totals = Math.round(totals * 100.0) / 100.0;

				fileWriter1.append("<p><b>Current Routes: </b>").append(String.valueOf(lengths.size())).append("<br>\n");
				fileWriter1.append("<b>Mean EDI: </b>").append(String.valueOf(meanEdi)).append("<br>\n");
				fileWriter1.append("<b>Median EDI: </b>").append(String.valueOf(medianEdi)).append("<br>\n");
				fileWriter1.append("<b>Mean Length: </b>").append(String.valueOf(meanLength)).append(" mi.<br>\n");
				fileWriter1.append("<b>Median Length: </b>").append(String.valueOf(medianLength)).append(" mi.</p>\n");

				lengths.clear();
				edis.clear();

				// check for sets
				try {
					// load in the sets
					ArrayList<String> agencySets = new ArrayList<>();
					Scanner t = new Scanner(new File("sets/" + agencies.get(a) + ".txt"));
					while (t.hasNextLine()) {
						String data = t.nextLine();
						agencySets.add(data);
					}

					// load in from the set
					// loop through all sets
					for (String agencySet : agencySets) {
						Scanner s = new Scanner(new File("edis/sets/" + agencies.get(a) + "-" + agencySet + ".txt"));
						while (s.hasNextLine()) {
							String data = s.nextLine();
							data = data.substring(data.indexOf(";") + 1);
							double miles = Double.parseDouble(data.substring(0, data.indexOf(";")));
							data = data.substring(data.indexOf(";") + 1);
							double edi = Double.parseDouble(data);

							lengths.add(miles);
							edis.add(edi);
							totals += miles;
						}

						Collections.sort(lengths);
						Collections.sort(edis);

						// mean EDI
						totalEdi = 0.0;
						for (Double edi : edis) {
							totalEdi = totalEdi + edi;
						}
						meanEdi = totalEdi / edis.size();
						meanEdi = Math.round(meanEdi * 100.0) / 100.0;

						// mean length
						totalLength = 0.0;
						for (Double length : lengths) {
							totalLength = totalLength + length;
						}
						meanLength = totalLength / lengths.size();
						meanLength = Math.round(meanLength * 100.0) / 100.0;

						// median EDI
						if (edis.size() % 2 == 1) // odd amount of EDIs
						{
							if (edis.size() == 1) {
								medianEdi = edis.get(0);
							} else {
								medianEdi = edis.get(edis.size() / 2);
							}
						} else // even amount of EDIs
						{
							if (edis.size() == 2) {
								medianEdi = (edis.get(0) + edis.get(1)) / 2.0;
							} else {
								medianEdi = (edis.get((edis.size() / 2) - 1) + edis.get(edis.size() / 2)) / 2.0;
							}
							medianEdi = Math.round(medianEdi * 100.0) / 100.0;
						}

						// median length
						if (lengths.size() % 2 == 1) // odd amount of lengths
						{
							if (lengths.size() == 1) {
								medianLength = lengths.get(0);
							} else {
								medianLength = lengths.get(lengths.size() / 2);
							}
						} else // even amount of lengths
						{
							if (lengths.size() == 2) {
								medianLength = (lengths.get(0) + lengths.get(1)) / 2.0;
							} else {
								medianLength = (lengths.get((lengths.size() / 2) - 1) + lengths.get(lengths.size() / 2)) / 2.0;
							}
							medianLength = Math.round(medianLength * 100.0) / 100.0;
						}

						// round total miles
						totals = Math.round(totals * 100.0) / 100.0;

						fileWriter1.append("<p><b>").append(agencySet).append(" Set</b><br>\n");
						fileWriter1.append("<b>Set Routes: </b>").append(String.valueOf(lengths.size())).append("<br>\n");
						fileWriter1.append("<b>Mean EDI: </b>").append(String.valueOf(meanEdi)).append("<br>\n");
						fileWriter1.append("<b>Median EDI: </b>").append(String.valueOf(medianEdi)).append("<br>\n");
						fileWriter1.append("<b>Mean Length: </b>").append(String.valueOf(meanLength)).append(" mi.<br>\n");
						fileWriter1.append("<b>Median Length: </b>").append(String.valueOf(medianLength)).append(" mi.</p>\n");

						lengths.clear();
						edis.clear();
					}
				} catch (Exception e) {
					// don't do a damn thing
				}
			}
			fileWriter1.close();
		} catch (Exception e) {
			System.out.println("Error creating stats page.");
		}
	}

	/**
	 * Generates stops.html
	 */
	static void stopList() {
		ArrayList<String> agencies = Util.getAgencyIds();
		ArrayList<String> fullAgencies = Util.getAgencyNames();

		int stopCounter = 0; // total amount of stops

		for (int i = 0; i < agencies.size(); i++) {
			// creates each agency page
			try {
				int agencyStops = 0; // agency stop counter
				File newFile1 = new File("public/stops/" + agencies.get(i) + ".html");
				FileWriter fileWriter1 = new FileWriter(newFile1);

				fileWriter1.write("<title>" + fullAgencies.get(i) + " Stops - Eliot Deviation Index</title> \n");
				fileWriter1.append("<link rel=stylesheet href=../style.css> \n");
				fileWriter1.append("<ul><li><a href=../index.html>Home</a></li>");
				fileWriter1.append("<li><a href=../stops.html class=active>Stop Listing</a></li> \n");
				fileWriter1.append("<li><a href=../routes.html>Route Listing</a></li> \n");
				fileWriter1.append("<li><a href=../detailed.html>Route Detail</a></li> \n");
				fileWriter1.append("<li><a href=../stats.html>Statistics</a></li> \n");
				fileWriter1.append("<li><a href=../calculator.html>Calculator</a></li></ul> \n");
				fileWriter1.append("<h1>Stop Listing</h1> \n");

				fileWriter1.append("<h3>").append(fullAgencies.get(i)).append(" (").append(agencies.get(i)).append(")</h3> \n");

				ArrayList<String> stopID = new ArrayList<>();
				ArrayList<String> stopName = new ArrayList<>();
				ArrayList<String> stopLat = new ArrayList<>();
				ArrayList<String> stopLon = new ArrayList<>();

				try {
					// port .txt list to webpage
					Scanner s = new Scanner(new File("stops/" + agencies.get(i) + ".txt"));
					while (s.hasNextLine()) {
						String data = s.nextLine();
						String id = data.substring(0, data.indexOf(";"));
						stopID.add(id);
						data = data.substring(data.indexOf(";") + 1);
						String name = data.substring(0, data.indexOf(";"));
						stopName.add(name);
						data = data.substring(data.indexOf(";") + 1);
						String lat = data.substring(0, data.indexOf(";"));
						stopLat.add(lat);
						data = data.substring(data.indexOf(";") + 1);
						String lon = data;
						stopLon.add(lon);
					}
				} catch (FileNotFoundException e) {
					System.out.println("Error, no stop file for " + agencies.get(i) + ".");
				}

				fileWriter1.append("<table><tr><th>Stop ID</th><th>Stop Name</th><th>Stop Latitude</th><th>Stop Longitude</th></tr> \n");

				for (int j = 0; j < stopID.size(); j++) {
					fileWriter1.append("<tr><td style=color:#1aab2d>").append(stopID.get(j)).append("</td><td>").append(stopName.get(j)).append("</td><td>").append(stopLat.get(j)).append("</td><td>").append(stopLon.get(j)).append("</td></tr> \n");
					agencyStops++;
					stopCounter++;
				}

				fileWriter1.append("</table> \n");
				fileWriter1.append("<p><b>Agency Stops:</b> ").append(String.valueOf(agencyStops)).append("</p>");
				fileWriter1.flush();
				fileWriter1.close();
				System.out.println("Agency Stops (" + agencies.get(i) + "): " + agencyStops);
			} catch (IOException e) {
				System.out.println("Error, can't save stops for " + agencies.get(i) + ".");
			}
		}

		// main stops home page
		try {
			File newFile2 = new File("public/stops.html");
			FileWriter fileWriter2 = new FileWriter(newFile2);
			fileWriter2.write("<title>Stop Listing - Eliot Deviation Index</title> \n");
			fileWriter2.append("<link rel=stylesheet href=style.css> \n");
			fileWriter2.append("<ul><li><a href=index.html>Home</a></li>");
			fileWriter2.append("<li><a href=stops.html class=active>Stop Listing</a></li> \n");
			fileWriter2.append("<li><a href=routes.html>Route Listing</a></li> \n");
			fileWriter2.append("<li><a href=detailed.html>Route Detail</a></li> \n");
			fileWriter2.append("<li><a href=stats.html>Statistics</a></li> \n");
			fileWriter2.append("<li><a href=calculator.html>Calculator</a></li></ul> \n");
			fileWriter2.append("<h1>Stop Listing</h1> \n");
			fileWriter2.append("<p> \n");

			for (int a = 0; a < agencies.size(); a++) {
				fileWriter2.append("<ul class=bullet style=background-color:#1f1f1f><a href=stops/").append(agencies.get(a)).append(".html>").append(fullAgencies.get(a)).append("</a> (").append(agencies.get(a)).append(")</ul> \n");
			}

			fileWriter2.append("<p><b>Total Stops: </b>").append(String.valueOf(stopCounter)).append("</p>");
			fileWriter2.flush();
			fileWriter2.close();
			System.out.println("Stop Count: " + stopCounter);
		} catch (Exception e) {
			System.out.println("Error, can't create stop home page.");
		}
	}

	@SuppressWarnings("unused")
	static void stopMap() {
		ArrayList<String> agencies = Util.getAgencyIds();

		for (String agency : agencies) {
			ArrayList<Stop> load = Util.getAgencyStops(agency);

			Kml kml = new Kml();
			Document document = kml.createAndSetDocument().withName(agency + " stops");

			Style style = document.createAndAddStyle().withId("stop");
			style.createAndSetIconStyle().withScale(0.5);

			for (Stop stop : load) {
				Placemark placemark = document.createAndAddPlacemark().withStyleUrl("#stop")
						.withDescription("Name: " + stop.getName() + "<br/>ID: " + stop.getId());
				placemark.createAndSetPoint().addToCoordinates(stop.getLon(), stop.getLat(), 0);
			}

			writeKmlFile(document, "public/maps/stops/map-" + agency + ".kml");
		}
	}
}
