package org.eliotindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

abstract class Util {
	static ArrayList<String> getAgencyIds() {
		ArrayList<String> agencies = new ArrayList<>();
		try {
			Scanner s = new Scanner(new File("agencies.txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				agencies.add(data.substring(0, data.indexOf(";")));
			}
		} catch (Exception e) {
			System.err.println("Error - no agencies.");
		}
		return agencies;
	}

	static ArrayList<String> getAgencyNames() {
		ArrayList<String> agencies = new ArrayList<>();
		try {
			Scanner s = new Scanner(new File("agencies.txt"));
			while (s.hasNextLine()) {
				String data = s.nextLine();
				agencies.add(data.substring(data.indexOf(";") + 1));
			}
		} catch (Exception e) {
			System.err.println("Error - no agencies.");
		}
		return agencies;
	}

	static ArrayList<Route> getAgencyEdis(String agency) {
		ArrayList<Route> routes = new ArrayList<>();

		try {
			Scanner s = new Scanner(new File("edis/" + agency + ".txt"));
			while (s.hasNextLine()) {
				String[] data = s.nextLine().split(";");

				String line = data[0];
				double edi = Double.parseDouble(data[2]);
				String name = data[3].replace("&", "&amp;");
				String branch = data[4].replace("&", "&amp;");

				routes.add(new Route(line, edi, name, branch));
			}
		} catch (Exception e) {
			System.err.println("Error: no EDI file for " + agency + ".");
		}

		return routes;
	}

	static ArrayList<Stop> getAgencyRouteStops(String agency) {
		ArrayList<Stop> routeStops = new ArrayList<>();

		try {
			Scanner s = new Scanner(new File("files/" + agency + "-edi.txt"));
			while (s.hasNextLine()) {
				String[] data = s.nextLine().split(";");

				String id = data[0];
				String name = data[1];
				double lat = Double.parseDouble(data[2]);
				double lon = Double.parseDouble(data[3]);
				String line = data[4];
				int order = Integer.parseInt(data[5]);

				routeStops.add(new Stop(agency, id, name, lat, lon, line, order));
			}
		} catch (Exception e) {
			return routeStops;
		}

		return routeStops;
	}

	static ArrayList<Stop> getAgencyStops(String agency) {
		ArrayList<Stop> stops = new ArrayList<>();

		try {
			Scanner s = new Scanner(new File("stops/" + agency + ".txt"));
			while (s.hasNextLine()) {
				String[] data = s.nextLine().split(";");

				String id = data[0];
				String name = data[1];
				double lat = Double.parseDouble(data[2]);
				double lon = Double.parseDouble(data[3]);

				stops.add(new Stop(id, name, lat, lon));
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error, can't load stop file " + agency + ".");
		}

		return stops;
	}

	@SuppressWarnings("SpellCheckingInspection")
	static ArrayList<String> mapInit(String title) {
		ArrayList<String> maps = new ArrayList<>();

		maps.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		maps.add("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
		maps.add("<Document> \n\t<name>" + title + "</name>");
		maps.add("\t<Style id=\"1.0\"> \n\t\t<LineStyle> \n\t\t\t<color>ff10c283</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"1.5\"> \n\t\t<LineStyle> \n\t\t\t<color>ff195c03</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"2.0\"> \n\t\t<LineStyle> \n\t\t\t<color>ffa0ad10</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"2.5\"> \n\t\t<LineStyle> \n\t\t\t<color>ffad4902</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"3.0\"> \n\t\t<LineStyle> \n\t\t\t<color>ffbf1d7e</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"3.5\"> \n\t\t<LineStyle> \n\t\t\t<color>ff6e0cb0</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"4.0\"> \n\t\t<LineStyle> \n\t\t\t<color>ff190177</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");
		maps.add("\t<Style id=\"10.0\"> \n\t\t<LineStyle> \n\t\t\t<color>ff000000</color> \n\t\t\t<width>4.0</width> \n\t\t</LineStyle> \n\t</Style>");

		return maps;
	}
}
