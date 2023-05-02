import java.sql.*;
import java.util.Scanner;
public class Lab4 {
	//login credentials
	public static final String USERNAME = "";
	public static final String PASSWORD = "";
	
	//create keyboard for input
	public static Scanner kb = new Scanner(System.in);


	public static void main(String[] args) {
		try
		{
			//establish connection
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/lab4", USERNAME, PASSWORD);
			Statement stmt = con.createStatement();

			int selection = 0;
			while (selection != -1)
			{
				//get user input
				selection = selectionPrompt();

				//determine action
				switch (selection) {
					case -1: //quit
						return;
					case 0: 
						displaySchedules(stmt);
						break;
					case 1:
						editSchedule(stmt);
						break;
					case 2:
						displayStops(stmt);
						break;
					case 3:
						displayDriverWeekly(stmt);
						break;
					case 4:
						addDriver(stmt);
						break;
					case 5:
						addBus(stmt);
						break;
					case 6:
						deleteBus(stmt);
						break;
					case 7:
						insertActualTripStopInfo(stmt);
						break;
					default:
						break;
				}
			}

			//close kb when done
			kb.close();
			//close connection
			con.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	//return user input
	public static int selectionPrompt()
	{
		//display options
		displayOptions();

		//get selection and clear line
		int selection = kb.nextInt();
		kb.nextLine();

		//accept input
		return selection;
	}
	
	//Display options
	public static void displayOptions()
	{
		System.out.println("(0) : Display schedule of all trips, given start location, destination, and date");
		System.out.println("(1) : Edit the schedule");
		System.out.println("(2) : Display the stops of a given trip");
		System.out.println("(3) : Display the weekly schedule of a given driver and date");
		System.out.println("(4) : Add a driver");
		System.out.println("(5) : Add a bus");
		System.out.println("(6) : Delete a bus");
		System.out.println("(7) : Record actual data of trip offering specified by its key");
		System.out.print("Input your selection (-1 to quit) : ");
	}

	//prompt user for a string
	public static String promptDetail(String prompt)
	{
		System.out.println(prompt);
		System.out.print("? ");
		return kb.nextLine();
	}

	/*
	 * Display the schedule of all trips for a given StartLocation and
	 * Destination Name, and Date. In addition to these attributes, the
	 * schedule includes: Scheduled StartTime, ScheduledArrivalTime,
	 * DriverID, and BusID.
	 */
	public static void displaySchedules(Statement stmt) throws SQLException
	{
		//prompt for required information
		String startLoc = promptDetail("Input Start Location");
		String destName = promptDetail("Input Destination name");
		String date = promptDetail("Input Date");

		//query
		ResultSet rs = stmt.executeQuery("SELECT t.ScheduledStartTime, t.ScheduledArrivalTime, t.DriverName, t.BusID"
			+"\nFROM trip_offering t"
			+"\n	JOIN trip tr ON tr.TripNumber = t.TripNumber"
			+"\nWHERE t.Date = \'" + date + "\' AND tr.StartLocationName LIKE \'"+ startLoc +"\' AND tr.DestinationName LIKE \'" + destName + "\';");
		
		//Output rows
		System.out.println("START\t|\tDEST\t|\tDATE\t\t|\tSTART TIME\t|\tARIVE TIME\t|\tDRIVER\t|\tBUS");
		while (rs.next())
		{
			System.out.println(startLoc + "\t|\t" + destName + "\t|\t" + date + "\t|\t" 
				+ rs.getTime("ScheduledStartTime", null) + "\t|\t" 
				+ rs.getTime("ScheduledArrivalTime", null) + "\t|\t" + rs.getString("DriverName") + "\t|\t" + rs.getInt("BusID"));
		}
	}

	/*
	 * Edit the schedule i.e. edit the table of Trip Offering as follows:
	 * 	- Delete a trip offering specified by Trip#, Date, and 
	 * 		ScheduledStartTime;
	 * 	- Add a set of trip offerings assuming the values of all 
	 * 		attributes are given (the software asks if you have more
	 * 		trips to enter);
	 * 	- Change the driver for a given Trip offering (i.e. given 
	 * 		TripNumber, Date, ScheduledStartTime);
	 * 	- Change the bus for a given Trip offering
	 */
	public static void editSchedule(Statement stmt) throws SQLException
	{
		int selection = 0;

		//ask for more edits until selection is -1
		while (selection != -1)
		{
			//get selection
			selection = editScheduleSelection();

			//determine action
			switch (selection) {
				case 0:
					deleteTrip(stmt);
					break;
				case 1:
					addTripOfferingSet(stmt);
					break;
				case 2:
					editDriverGivenTripOffering(stmt);
					break;
				case 3:
					editBusGivenTrip(stmt);
					break;
				default:
					break;
			}
		}
	}

	//get selection for specific type of schedule edit
	public static int editScheduleSelection()
	{
		//display options
		displayEditOptions();

		//get selection and clear line
		int selection = kb.nextInt();
		kb.nextLine();

		//accept input
		return selection;
	}

	//display options for schedule edit
	public static void displayEditOptions()
	{
		System.out.println("(0) : Delete a trip");
		System.out.println("(1) : Add a set of trip offerings");
		System.out.println("(2) : Change driver for a given trip");
		System.out.println("(3) : Change bus for a given trip");
		System.out.print("Input your selection (-1 to quit) : ");
	}
	
	//Delete a trip offering specified by Trip#, Date, and ScheduledStartTime;
	public static void deleteTrip(Statement stmt) throws SQLException
	{
		//prompt for required information
		String tripNum = promptDetail("Input Trip #");
		String date = promptDetail("Input Date");
		String startTime = promptDetail("Input Start Time");

		//query
		stmt.executeUpdate("DELETE FROM trip_offering" 
		+ " WHERE TripNumber = \'" + tripNum + "\'" 
		+ " AND Date = \'" + date + "\'"
		+ " AND ScheduledStartTime = \'" + startTime + "\'");
	}

	//Add a set of trip offerings assuming the values of all attributes are given (the software asks if you have more trips to enter);
	public static void addTripOfferingSet(Statement stmt) throws SQLException
	{
		//default to yes add one more
		String more = "Y";
		
		while (more.equals("Y"))
		{
			//prompt for required information
			String tripNum = promptDetail("Input Trip #");
			String date = promptDetail("Input Date");
			String startTime = promptDetail("Input Start Time");
			String arrivTime = promptDetail("Input Arrival Time");
			String driverName = promptDetail("Input Driver Name");
			String busID = promptDetail("Input Bus ID");

			//query to add set
			stmt.executeUpdate("INSERT INTO trip_offering (TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime, DriverName, BusID)"
				+ "\nVALUES ("+ tripNum + ", \'"+ date +"\', \'"+ startTime +"\', \'"+ arrivTime +"\', \'"+ driverName +"\', "+ busID +");");

			//ask if user wants to continue
			more = promptDetail("More trips to enter? (Y/N): ");
		}
	}

	//Change the driver for a given Trip offering (i.e. given TripNumber, Date, ScheduledStartTime);
	public static void editDriverGivenTripOffering(Statement stmt) throws SQLException
	{
		//prompt for required information
		String tripNum = promptDetail("Input Trip #");
		String date = promptDetail("Input Date");
		String startTime = promptDetail("Input Start Time");
		String driverName = promptDetail("Input Driver Name");

		//change row
		stmt.executeUpdate("UPDATE trip_offering"
		+"\nSET DriverName = \'"+ driverName +"\'"
		+"WHERE TripNumber = "+ tripNum +" AND Date = \'"+ date + "\' AND ScheduledStartTime = \'"+ startTime +"\';");
	}

	//Change the bus for a given Trip offering
	public static void editBusGivenTrip(Statement stmt) throws SQLException
	{
		//prompt for required information
		String tripNum = promptDetail("Input Trip #");
		String date = promptDetail("Input Date");
		String startTime = promptDetail("Input Start Time");
		String busID = promptDetail("Input Bus ID");

		//change row
		stmt.executeUpdate("UPDATE trip_offering"
		+"\nSET BusID = " + busID
		+"\nWHERE TripNumber = "+ tripNum +" AND Date = \'"+ date + "\' AND ScheduledStartTime = \'"+ startTime +"\';");
	}

	//display the stops of a given trip (i.e. the attributes of TripStopInfo)
	public static void displayStops(Statement stmt) throws SQLException
	{
		//prompt for required information
		String tripNum = promptDetail("Input Trip #");

		//Query
		ResultSet rs = stmt.executeQuery("SELECT *"
		+"\nFROM trip_stop_info"
		+	"\n\tJOIN trip on trip.TripNumber = trip_stop_info.TripNumber"
		+"\nWHERE trip.TripNumber = "+ tripNum +";");

		//Output rows
		System.out.println("TNUM\t|\tSNUM\t|\tSEQ\t|\tDRVTIME");
		while (rs.next())
		{
			System.out.println(tripNum + "\t|\t"
				+ rs.getInt("StopNumber") + "\t|\t" + rs.getInt("SequenceNumber") + "\t|\t"
				+ rs.getTime("DrivingTime", null));
		}
	}

	//display the weekly schedule of a given driver and date
	public static void displayDriverWeekly(Statement stmt) throws SQLException
	{
		//prompt for required information
		String driverName = promptDetail("Input Driver Name");
		String date = promptDetail("Input Date");

		//Query
		ResultSet rs = stmt.executeQuery("SELECT *"
		+"\nFROM trip_offering"
		+"\nJOIN driver on driver.DriverName = trip_offering.DriverName"
		+"\nWHERE trip_offering.Date = \'"+ date +"\' AND trip_offering.DriverName = \'"+ driverName +"\';");

		//Output rows
		System.out.println("NAME\t|\tDATE\t\t|\tTNUM\t|\tSTART\t\t|\tARRIV\t\t|\tBUS");
		while (rs.next())
		{
			System.out.println(driverName + "\t|\t" + date + "\t|\t"
				+ rs.getInt("TripNumber") + "\t|\t" + rs.getTime("ScheduledStartTime", null) + "\t|\t"
				+ rs.getTime("ScheduledArrivalTime", null) + "\t|\t" + rs.getInt("BusID"));
		}
	}

	//add a driver
	public static void addDriver(Statement stmt) throws SQLException
	{
		//prompt for required information
		String driverName = promptDetail("Input Driver Name");
		String phoneNum = promptDetail("Input Driver's Telephone #");

		//query to add driver
		stmt.executeUpdate("INSERT INTO driver (DriverName, DriverTelephoneNumber)"
		+ "\nVALUES (\'"+ driverName + "\', \'"+ phoneNum +"\');");
	}

	//add a bus
	public static void addBus(Statement stmt) throws SQLException
	{
		//prompt for required information
		String busID = promptDetail("Input Bus ID");
		String model = promptDetail("Input Bus Model");
		String year = promptDetail("Input Bus Year");

		//query to add bus
		stmt.executeUpdate("INSERT INTO bus (BusID, Model, Year)"
		+ "\nVALUES ("+ busID + ", \'"+ model + "\', " + year +");");
	}

	//delete a bus
	public static void deleteBus(Statement stmt) throws SQLException
	{
		//prompt for required information
		String busID = promptDetail("Input Bus ID");
		String model = promptDetail("Input Bus Model");
		String year = promptDetail("Input Bus Year");

		//query to delete bus
		stmt.executeUpdate("DELETE FROM bus"
		+ "\nWHERE BusID = "+ busID +" AND Model = \'"+ model +"\' AND Year = "+ year +";");
	}

	//basically, "make an entry in TripOffering based on the details in ActualTripStopInfo"
	public static void insertActualTripStopInfo(Statement stmt) throws SQLException
	{
		//prompt for required information
		String tripNum = promptDetail("Input Trip #");

		//query to get data
		ResultSet rs = stmt.executeQuery("SELECT * "
		+"\nFROM actual_trip_stop_info "
		+"\nWHERE TripNumber = "+ tripNum +";");

		//get the data
		Date date = null;
		Time start = null;
		Time arrival = null;
		if (rs.next())
		{
			date = rs.getDate("Date", null);
			start = rs.getTime("ScheduledStartTime", null);
			arrival = rs.getTime("ScheduledArrivalTime", null);
		}

		//query to add trip_offering
		stmt.executeUpdate("INSERT INTO trip_offering (TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime)"
		+ "\nVALUES ("+ tripNum + ", \'"+ date + "\', \'" + start + "\', \'" + arrival + "\');");
	}
}
