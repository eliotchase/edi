import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;

// the thing that calculates the Eliot Deviation Index

public class ediCalc
{
  static String agencyChoice;
  public static void main(String [] args)
  {
    Scanner a = new Scanner(System.in); // for agency choice
    // intro

    System.out.println("Welcome to the Eliot Deviation Index calculator!");
    System.out.println("");
	 
	  System.out.println("Before starting, please select the agency you would like to access data for with the following format: [state abbreviation]-[agency name], e.g. ma-mbta");

    System.out.println("");
    
    System.out.println("INSTRUCTIONS - EXISTING LINES");
    System.out.println("Enter line name when prompted. If the line doesn't exist, don't panic, let Eliot know that you want it.");

    System.out.println("");

    System.out.println("INSTRUCTIONS - CUSTOM LINES");
    System.out.println("- Enter \"custom\" when prompted for the line.");
    System.out.println("- Enter the stop IDs in order. A full list can be found at edi.benchase.info/stops.html.");
    System.out.println("- Enter \"-1\" to add a custom stop.");
    System.out.println("- Enter \"-2\" to remove the last stop added.");
    System.out.println("- Enter \"-0\" after the last stop is added.");

    System.out.println("");

    System.out.println("BONUS FEATURES");
    System.out.println("- Enter \"segment\" to find the Eliot Deviation Index of a segment of a line. You will need the stop orders from the full line.");
    System.out.println("- Enter \"reverse\" to reverse the order of the line.");
    
    System.out.println("");

    System.out.println("Enjoy!");
    System.out.println("- Eliot");

    System.out.println("");
    agency();
  }

  public static void agency()
  {
    Scanner a = new Scanner(System.in);
    System.out.print("Agency: ");

    // check if valid agency
    ArrayList<String> agencies = new ArrayList<String>();
    try
    {
      Scanner s = new Scanner(new File("agencies.txt"));
      while (s.hasNextLine())
      {
        String data = s.nextLine();
        agencies.add(data.substring(0, data.indexOf(";")));
      }
    }
    catch (Exception e)
    {
      System.out.println("Error - no agencies.");
    }

    agencyChoice = a.nextLine();
    boolean actualAgency = false;
    for (int i = 0; i < agencies.size(); i++)
    {
      if (agencies.get(i).equalsIgnoreCase(agencyChoice))
      {
        actualAgency = true;
        edi();
      }
    }
    if (!actualAgency)
    {
      System.out.print("Invalid agency. ");
      agency();
    }
  }

  public static void edi()
  {
    int cont = 1; // 1 to continue

    while (cont == 1)
    {
      ArrayList<Stop> stops = new ArrayList<Stop>();

      // create line
      int stopCount = 0;

      Scanner in = new Scanner(System.in);
      System.out.print("Enter line: ");
      String lineChoice = in.nextLine();
      String lineName = "no data, yet";
      boolean official = false;
      boolean saved = false; // updates when asked to save
      String creatorName = "eliot"; // lazy to fix

      Stop [] theLine;

      if (lineChoice.equalsIgnoreCase("custom"))
      {
        // load in only existing stops
        try
        {
          Scanner s = new Scanner(new File("stops/" + agencyChoice + ".txt"));
          while (s.hasNextLine())
          {
            String data = s.nextLine();
            String id = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            String name = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            double lat = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1);
            double lon = Double.parseDouble(data);

            stops.add(new Stop(id, name, lat, lon));
          }
        }
        catch (Exception e)
        {
          System.out.println("Error. can't load stops");
        }

        ArrayList<Stop> stops2 = new ArrayList<Stop>(); // add to EDI list instead

        ArrayList<Stop> custom = new ArrayList<Stop>(); // array for custom line
        String customStop = "";

        System.out.print("Line Name: ");
        lineName = in.nextLine();
        official = true;
        
        while (!customStop.equals("-0"))
        {
          System.out.print("Stop ID: ");
          customStop = in.nextLine();

          // custom stop location - will not add to agency listing, use addStop
          if (customStop.equals("-1"))
          {
            stopCount++;
            System.out.print("New Stop Name: ");
            String custName = in.nextLine();
            System.out.print("New Stop ID: ");
            String custID = in.nextLine();
            System.out.print("New Stop Latitude: ");
            double custLat = in.nextDouble();
            System.out.print("New Stop Longitude: ");
            double custLon = in.nextDouble();

            Stop addStop = new Stop(custID, custName, custLat, custLon, lineName, stopCount);

            stops2.add(addStop);
            custom.add(addStop);

            in.nextLine(); // absorb enter
          }
          if (customStop.equals("-2"))
          {
            System.out.println(custom.get(custom.size() - 1).getName() + " (" + custom.get(custom.size() - 1).getID() + ") removed.");
            stops2.remove(stops2.size() - 1);
            custom.remove(custom.size() - 1);
            stopCount--;
          }

          // search for stop in listing
          else
          {
            for (int i = 0; i < stops.size(); i++)
            {
              if (stops.get(i).getID().equalsIgnoreCase(customStop))
              {
                stopCount++;
                System.out.print("Added: " + stops.get(i).getName() + " "); // space for the stupidity
                Stop addStop = new Stop(stops.get(i).getID(), stops.get(i).getName(), stops.get(i).getLat(), stops.get(i).getLon(), lineName, stopCount);
                stops2.add(addStop);
                custom.add(stops.get(i));
                break;
              }          
            }
          }
          if (!customStop.equals("-0"))
          {
            rollingEDI(custom); // this is where shit gets stupid
          }
        }

        theLine = new Stop[custom.size()];
        for (int i = 0; i < custom.size(); i++)
        {
          theLine[i] = custom.get(i);
        }

        System.out.print("Save line? ");
        String save = in.nextLine();
        if (save.equalsIgnoreCase("yes"))
        {
          saved = true;
          // loads in EDI file to add route to list, different array
          try
          {
            Scanner s2 = new Scanner(new File(agencyChoice + "-edi.txt"));
            while (s2.hasNextLine())
            {
              String data = s2.nextLine();
              String id = data.substring(0, data.indexOf(";"));
              data = data.substring(data.indexOf(";") + 1);
              String name = data.substring(0, data.indexOf(";"));
              data = data.substring(data.indexOf(";") + 1);
              double lat = Double.parseDouble(data.substring(0, data.indexOf(";")));
              data = data.substring(data.indexOf(";") + 1);
              double lon = Double.parseDouble(data.substring(0, data.indexOf(";")));
              data = data.substring(data.indexOf(";") + 1); // lines
              String line = data.substring(0, data.indexOf(";"));
              data = data.substring(data.indexOf(";") + 1);
              int order = Integer.parseInt(data);

              stops2.add(new Stop(id, name, lat, lon, line, order));
            }
          }
          catch (Exception e)
          {
            System.out.println("Error.");
          }

          // adds list to -edi file, allows it to be read by calculator
          try
          {
            File newFile1 = new File(agencyChoice + "-edi.txt");
            FileWriter fileWriter1 = new FileWriter(newFile1);

            fileWriter1.write(stops2.get(0).getID() + ";" + stops2.get(0).getName() + ";" + stops2.get(0).getLat() + ";" + stops2.get(0).getLon() + ";" + stops2.get(0).getLineEDI() + ";" + stops2.get(0).getOrder() + "\n");

            for (int i = 1; i < stops2.size(); i++)
            {
              fileWriter1.append(stops2.get(i).getID() + ";" + stops2.get(i).getName() + ";" + stops2.get(i).getLat() + ";" + stops2.get(i).getLon() + ";" + stops2.get(i).getLineEDI() + ";" + stops2.get(i).getOrder() + "\n");
            }

            fileWriter1.close();

            System.out.println("Line added.");
          }
          catch (Exception e)
          {
            System.out.println("Error.");
          }
        }
      }

      // segments of routes
      else if (lineChoice.equalsIgnoreCase("segment"))
      {
        // loads in EDI file with existing routes only
        try
        {
          Scanner s = new Scanner(new File(agencyChoice + "-edi.txt"));
          while (s.hasNextLine())
          {
            String data = s.nextLine();
            String id = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            String name = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            double lat = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1);
            double lon = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1); // lines
            String line = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            int order = Integer.parseInt(data);

            stops.add(new Stop(id, name, lat, lon, line, order));
          }
        }
        catch (Exception e)
        {
          System.out.println("Error.");
        }
        System.out.print("Enter line: ");
        lineChoice = in.nextLine();
        System.out.print("Starting stop: ");
        int startStop = in.nextInt();
        System.out.print("Ending stop: ");
        int endStop = in.nextInt();

        // check if in part of line desired
        for (int i = 0; i < stops.size(); i++)
        {
          if (stops.get(i).getLineEDI().equalsIgnoreCase(lineChoice) && stops.get(i).getOrder() <= endStop && stops.get(i).getOrder() >= startStop)
          {
            stopCount++;
          }
        }

        if (stopCount == 0)
        {
          System.out.print("Invalid segment. ");
          edi();
          break;
        }

        theLine = new Stop[stopCount];
        for (int i = 0; i < stops.size(); i++)
        {
          if (stops.get(i).getLineEDI().equalsIgnoreCase(lineChoice) && stops.get(i).getOrder() <= endStop && stops.get(i).getOrder() >= startStop)
          {
            theLine[stops.get(i).getOrder() - startStop] = stops.get(i);
          }
        }
      }

      else if (lineChoice.equalsIgnoreCase("reverse")) // reverses order
      {
        // loads in EDI file with existing routes only
        try
        {
          Scanner s = new Scanner(new File(agencyChoice + "-edi.txt"));
          while (s.hasNextLine())
          {
            String data = s.nextLine();
            String id = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            String name = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            double lat = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1);
            double lon = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1); // lines
            String line = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            int order = Integer.parseInt(data);

            stops.add(new Stop(id, name, lat, lon, line, order));
          }
        }
        catch (Exception e)
        {
          System.out.println("Error.");
        }

        System.out.print("Enter line: ");
        lineChoice = in.nextLine();

        for (int i = 0; i < stops.size(); i++)
        {
          if (stops.get(i).getLineEDI().equalsIgnoreCase(lineChoice))
          {
            stopCount++;
          }
        }

        if (stopCount == 0)
        {
          System.out.print("Invalid line. ");
          edi();
          break;
        }

        // reversing using temp array
        Stop [] tempLine = new Stop[stopCount];
        theLine = new Stop[stopCount];
        for (int i = 0; i < stops.size(); i++)
        {
          if (stops.get(i).getLineEDI().equalsIgnoreCase(lineChoice))
          {
            tempLine[stops.get(i).getOrder() - 1] = stops.get(i);
          }
        }

        int remain = tempLine.length;
        for (int i = 0; i < tempLine.length; i++)
        {
          theLine[i] = tempLine[remain - 1];
          remain--;
        }
      }

      // find route in database
      else
      {
        // loads in EDI file with existing routes only
        try
        {
          Scanner s = new Scanner(new File(agencyChoice + "-edi.txt"));
          while (s.hasNextLine())
          {
            String data = s.nextLine();
            String id = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            String name = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            double lat = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1);
            double lon = Double.parseDouble(data.substring(0, data.indexOf(";")));
            data = data.substring(data.indexOf(";") + 1); // lines
            String line = data.substring(0, data.indexOf(";"));
            data = data.substring(data.indexOf(";") + 1);
            int order = Integer.parseInt(data);

            stops.add(new Stop(id, name, lat, lon, line, order));
          }
        }
        catch (Exception e)
        {
          System.out.println("Error.");
        }

        // check if stop on line
        for (int i = 0; i < stops.size(); i++)
        {
          if (stops.get(i).getLineEDI().equalsIgnoreCase(lineChoice))
          {
            stopCount++;
          }
        }

        if (stopCount == 0)
        {
          System.out.print("Invalid line. ");
          edi();
          break;
        }

        theLine = new Stop[stopCount];
        for (int i = 0; i < stops.size(); i++)
        {
          if (stops.get(i).getLineEDI().equalsIgnoreCase(lineChoice))
          {
            theLine[stops.get(i).getOrder() - 1] = stops.get(i);
          }
        }
      }

      // rolling EDI for each stop added
      for (int i = 0; i < theLine.length; i++)
      {
        System.out.print("- " + theLine[i].getName() + " (" + theLine[i].getID() + ") ");
        ArrayList<Stop> custom2 = new ArrayList<Stop>(); // array for rolling EDI
        for (int j = 0; j <= i; j++)
        {
          custom2.add(theLine[j]);
        }
        rollingEDI(custom2);
      }

      // haversine formula loop
      // spherical trig cause this is the globe
      double dist = 0;
      for (int i = 1; i < theLine.length; i++)
      {
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
      dist = ((int)(dist * 100)) / 100.00;
      System.out.println("Distance: " + dist + " miles");

      // full line haversine
      double firstLon = Math.toRadians(Math.abs(theLine[0].getLon()));
      double lastLon = Math.toRadians(Math.abs(theLine[theLine.length  - 1].getLon())); // 11 is temp
      double firstLat = Math.toRadians(Math.abs(theLine[0].getLat()));
      double lastLat = Math.toRadians(Math.abs(theLine[theLine.length - 1].getLat())); // 11 is temp
      double difflon = lastLon - firstLon;
      double difflat = lastLat - firstLat;
      double a1 = Math.pow(Math.sin(difflat / 2), 2) + Math.cos(firstLat) * Math.cos(lastLat) * Math.pow(Math.sin(difflon / 2), 2);      
      double c1 = 2 * Math.asin(Math.sqrt(a1));
      double r1 = 3963;
      double lineDist = c1 * r1;

      // calculate the edi
      double edi = dist / lineDist;
      edi = ((int)(edi * 100)) / 100.00;

      if (edi < 1) // case for the 0.9x
      {
        edi = 1.0;
      }

      System.out.println("Eliot Deviation Index: " + edi);

      // this is when things get added to site listing
      if (official && saved)
      {
        ArrayList<String> routeCode = new ArrayList<String>();
        ArrayList<String> routeDist = new ArrayList<String>();
        ArrayList<String> routeEdi = new ArrayList<String>();

        // add to .txt file, gets converted in routeList
        try
        {
          Scanner s = new Scanner(new File("edis/" + agencyChoice + ".txt"));
          while (s.hasNextLine())
          {
            String data = s.nextLine();
            String code = data.substring(0, data.indexOf(";"));
            routeCode.add(code);
            data = data.substring(data.indexOf(";") + 1);
            String dist2 = data.substring(0, data.indexOf(";"));
            routeDist.add(dist2);
            data = data.substring(data.indexOf(";") + 1);
            String ediA = data;
            routeEdi.add(ediA);
          }
        }
        catch (Exception e)
        {
          System.out.println("Error, no EDI list file for agency " + agencyChoice + "."); // expected error if first route
        }

        routeCode.add(lineName);
        routeDist.add(dist + "");
        routeEdi.add(edi + "");

        // add EDI to list.
        try
        {
          File newFile1 = new File("edis/" + agencyChoice + ".txt");
          FileWriter fileWriter1 = new FileWriter(newFile1);

          fileWriter1.write(routeCode.get(0) + ";" + routeDist.get(0) + ";" + routeEdi.get(0) + "\n");

          for (int b = 1; b < routeCode.size(); b++)
          {
            fileWriter1.append(routeCode.get(b) + ";" + routeDist.get(b) + ";" + routeEdi.get(b) + "\n");
          }

          fileWriter1.close();

          System.out.println("Line added.");
        }
        catch (Exception e)
        {
          System.out.println("Error.");
        }
      }

      System.out.print("Enter 1 to search again: ");
      cont = in.nextInt();
    }
  }

  // rolling calculator for custom lines, this is a stupid idea but it's 1 am and i feel like adding it
  public static void rollingEDI(ArrayList<Stop> custom)
  {
    Stop [] theLine = new Stop[custom.size()];
    for (int i = 0; i < custom.size(); i++)
    {
      theLine[i] = custom.get(i);
    }

    // haversine formula loop
    double dist = 0;
    for (int i = 1; i < theLine.length; i++)
    {
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
    dist = ((int)(dist * 100)) / 100.00;
    System.out.print("(" + theLine.length + ", " + dist + ", ");

    // full line haversine
    double firstLon = Math.toRadians(Math.abs(theLine[0].getLon()));
    double lastLon = Math.toRadians(Math.abs(theLine[theLine.length  - 1].getLon())); // 11 is temp
    double firstLat = Math.toRadians(Math.abs(theLine[0].getLat()));
    double lastLat = Math.toRadians(Math.abs(theLine[theLine.length - 1].getLat())); // 11 is temp
    double difflon = lastLon - firstLon;
    double difflat = lastLat - firstLat;
    double a1 = Math.pow(Math.sin(difflat / 2), 2) + Math.cos(firstLat) * Math.cos(lastLat) * Math.pow(Math.sin(difflon / 2), 2);      
    double c1 = 2 * Math.asin(Math.sqrt(a1));
    double r1 = 3963;
    double lineDist = c1 * r1;

    // calculate the edi
    double edi = dist / lineDist;
    edi = ((int)(edi * 100)) / 100.00;

    if (edi < 1) // case for the 0.9x
    {
      edi = 1.0;
    }

    System.out.println(edi + ")");
  }
}