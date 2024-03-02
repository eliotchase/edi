package org.eliotindex;

import java.util.Scanner;

public class Application {
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);

		System.out.println("Eliot Deviation Index Application");
		System.out.println("---------------------------------");
		System.out.println();
		System.out.println("Select an option:");
		System.out.println("- calc: The calculator for the Eliot Deviation Index");
		System.out.println("- search: Search the current stop database");
		System.out.println();
		System.out.print("Choice: ");

		String choice = s.nextLine();

		switch (choice.toLowerCase()) {
			case "add" -> AddStop.add();
			case "calc" -> Calculator.run();
			case "codetoname" -> CodeToName.run();
			case "hasindex" -> HasIndex.run();
			case "importindex" -> ImportIndex.run();
			case "request" -> Request.run();
			case "search" -> StopSearch.run();
			case "segment" -> Segment.run();
			case "stopinfo" -> StopInfo.run();
			case "update" -> Update.update();
		}
	}
}
