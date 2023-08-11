package com.wrhstuff.voter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Person {

	String firstName = "";
	String lastName = "";
	Integer politics = -1;
	double lat = 0.0;
	double lon = 0.0;

}

class Room {

	ArrayList<Person> persons = new ArrayList<Person>();
	String doorNum = "";

}

class Multi {

	ArrayList<Room> rooms = new ArrayList<Room>();
	String streetNum = "";

}

class House {

	ArrayList<Person> persons = new ArrayList<Person>();
	String streetNum = "";

}

class Street {

	ArrayList<House> houses = new ArrayList<House>();
	ArrayList<Multi> multis = new ArrayList<Multi>();
	String preDirection = "";
	String name = "";
	String type = "";
	String postDirection = "";
	String city = "";
	String zipCode = "";
	String state = "";
	Boolean isHouse = false;
	String fullStreet = "";

	public void Print() {

		System.out.println();
		System.out.println("New Street");
		System.out.println("----------");
		System.out.println("Homes = " + houses.size());
		System.out.println("Appartments = " + multis.size());
		System.out.println("preDirection = " + preDirection);
		System.out.println("name = " + name);
		System.out.println("type = " + type);
		System.out.println("postDirection = " + postDirection);
		System.out.println("city = " + city);
		System.out.println("zipCode = " + zipCode);
		System.out.println("state = " + state);
		System.out.println("isHouse = " + isHouse);
		System.out.println("fullStreet = " + fullStreet);
		System.out.println();

	}

	public Street(String data) {

		String[] list = data.split("\\|");

		String firstName = list[0];
		String lastName = list[1];
		preDirection = list[2];
		String streetNum = list[3];
		name = list[4];
		type = list[5];
		postDirection = list[6];
		String doorNum = list[7];
		city = list[8];
		zipCode = list[9];
		state = list[10];
		fullStreet = preDirection + name + type + postDirection + city + state;

		if (doorNum.equals("")) {
			isHouse = true;
		} else {
			isHouse = false;
		}

		Person person = new Person();
		person.firstName = firstName;
		person.lastName = lastName;

		if (isHouse) {
			if (houses.size() > 0) {
				House currentHouse = houses.get(houses.size() - 1);
				if (currentHouse.streetNum.equals(streetNum)) {
					currentHouse.persons.add(person);
				} else {
					House newHouse = new House();
					newHouse.streetNum = streetNum;
					newHouse.persons.add(person);
					houses.add(newHouse);
				}
			} else {
				House newHouse = new House();
				newHouse.streetNum = streetNum;
				newHouse.persons.add(person);
				houses.add(newHouse);
			}
		}

		else {
			if (multis.size() > 0) {
				Multi currentMulti = multis.get(multis.size() - 1);
				if (currentMulti.streetNum.equals(streetNum)) {
					if (currentMulti.rooms.size() > 0) {
						Room currentRoom = currentMulti.rooms.get(currentMulti.rooms.size() - 1);
						if (currentRoom.doorNum.equals(doorNum)) {
							currentRoom.persons.add(person);
						}
					}
				} else {
					Multi newMulti = new Multi();
					newMulti.streetNum = streetNum;
					Room newRoom = new Room();
					newRoom.persons.add(person);
					newMulti.rooms.add(newRoom);
					multis.add(newMulti);
				}
			} else {
				Multi newMulti = new Multi();
				newMulti.streetNum = streetNum;
				Room newRoom = new Room();
				newRoom.persons.add(person);
				newMulti.rooms.add(newRoom);
				multis.add(newMulti);
			}

		}

	}

}

// main class definition 
public class Voter {

	static ArrayList<Street> streets = new ArrayList<Street>();
	static Street street;

	// scrape screen of external site - pass URL
	private static String Scrape(String url)  {

		// build URL object
		URL obj;
		int responseCode = 0;
		HttpURLConnection httpURLConnection = null;
		
		try {
			obj = new URL(url);

			// call to external web site
			httpURLConnection = (HttpURLConnection) obj.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
			responseCode = httpURLConnection.getResponseCode();

		
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("URL = " + url.toString());


		// if connection okay, return source code of page, else wait 5 minutes and try
		// again. If still fail, return message
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			StringBuffer response = new StringBuffer();
			System.out.println("response code = " + responseCode);
			System.out.println("okay");
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String inputLine;
			try {
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("value = " + response.toString());
			return response.toString();
		} else {
			System.out.println("GET request not worked " + responseCode + " : " + url);
			if (responseCode == 403) {
				System.out.println("403 (forbiden) Sleeping...");
				try {
					Thread.sleep(240 * 1000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}

			if (responseCode == 400) {
				System.out.println("400 (bad form)");
			}

			return "";
		}
	}

	// method to reach out to external web site and get list
	private static ArrayList<String> Extract(String data, String search) {

		// System.out.println("URL = " + data);

		ArrayList<String> list = new ArrayList<String>();

		// string to hold external web site source code
		String myData = null;

		myData = Scrape(data);

		if (!myData.equals("")) {
			// split up web page source code based on passed string "search"
			String[] parts = myData.toUpperCase().split(search.toUpperCase());

			// split each "part" further and load into "list" list array
			for (String part : parts) {
				//System.out.println("part = " + part);
				String[] sections = part.split("-");
				if (sections[0].length() < 20)
					list.add(sections[0]);
				System.out.println("Section = " + sections[0]);
			}
		}
		return list;

	}

	// get list from single address? WRH
	private static ArrayList<String> PullList(Street street) {

		// System.out.println("Pull List");
		// street.Print();

		ArrayList<String> stuff = new ArrayList<String>();

		// link to external address and build URL
		String url = "https://www.spokeo.com/";

		if (street.preDirection != "")
			url += street.preDirection + "+";

		if (street.name != "")
			url = url + street.name + "+";

		if (street.type != "")
			url = url + street.type + "+";

		if (street.postDirection != "")
			url = url + street.postDirection + "+";

		if (street.city != "")
			url = url + street.city + "+";

		if (street.state != "")
			url = url + street.state + "+addresses";

		ArrayList<String> list = Extract(url, "https://www.spokeo.com//" + street.state + "/" + street.city + "/");
		System.out.println(url + " List size = " + list.size());
		System.out.println("First time, First item = " + list.get(0));
		for (String entry : list) {
			stuff.add(entry);
		}

		boolean done = false;
		if (list.size() < 50) {
			done = true;
		}

		int x = 1;
		while (!done) {
			x++;
			list.clear();
			list = Extract(url + "+" + x, "https://www.spokeo.com//" + street.state + "/" + street.city + "/");
			System.out.println(url  + "+" + x + " List size = " + list.size());
			System.out.println("Next time, First item = " + list.get(0));
			
			if (list.size() > 0) {
				if (stuff.get(0).equals(list.get(0))) {
					done = true;
				} else {
					for (String entry : list) {
						stuff.add(entry);
					}
				}
			}
			
			if (list.size() < 50) {
				done = true;
			}
			
			if (x > 500)
				done = true;
		}

		return stuff;
	}

	private static Street AddList(Street street, ArrayList<String> list) {

		ArrayList<String> source = new ArrayList<String>();

		for (House house : street.houses) {
			source.add(house.streetNum);
		}

		for (String entry : list) {
			if (source.contains(entry)) {
				// do nothing
			} else {
				Person person = new Person();
				person.firstName = "Unregistered";
				person.lastName = "Unregistered";
				House newHouse = new House();
				newHouse.streetNum = entry;
				newHouse.persons.add(person);
				street.houses.add(newHouse);
			}
		}
		return street;
	}

	private static void ReadData(String file) {

		BufferedReader br = null;

		try {
			// link to data file
			String strLine;
			br = new BufferedReader(new FileReader(file));
			String previousFullStreet = "";

			while ((strLine = br.readLine()) != null) {
				street = new Street(strLine);
//				street.Print();
				if (!street.fullStreet.equals(previousFullStreet)) {
					System.out.println("Adding " + street.fullStreet);
					streets.add(street);
				}
				previousFullStreet = street.fullStreet;
			}

			for (Street str : streets) {
				ArrayList<String> list = PullList(str);
				str = AddList(str, list);
			}

		} catch (IOException exp) {
			System.out.println("Error while reading input file " + exp.getMessage());
		} finally {
			try {
				// Close the stream
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static void WriteData(String file) {

		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter pw = new PrintWriter(fw);

		// printout houses
		// printout addresses
		pw.println("test");

		pw.close();

	}

	// call to main
	public static void main(String[] args) throws IOException { // TODO Auto-generated method stub


		String url = "https://www.spokeo.com/23RD+AVE+SE+BOTHELL+WA+addresses+3";
		ArrayList<String> list = Extract(url, "https://www.spokeo.com//" + "WA" + "/" + "BOTHELL" + "/");
		System.out.println(url + " List size = " + list.size());
		System.out.println("Next time, First item = " + list.get(0));

		
		/*
		ReadData("D:\\Database\\ld33.txt");
		WriteData("D:\\Database\\ld33_out_test.txt");
	*/
		/*
		 * 
		 * String[] arrayOfLine = {"123.456" ,
		 * "I.3 Accounting Terms","Including all","II.1 The Loans","II.3 Prepayments."
		 * ,"III.2 Illegality","IV.2 Conditions","V.2 Authorization","expected to have"
		 * }; Pattern pat = Pattern.compile("\\d\\d\\d.\\d\\d\\d"); List<String>
		 * listOfHeadings = new ArrayList<>(); for (String s : arrayOfLine) { Matcher m
		 * = pat.matcher(s); if (m.find()) { listOfHeadings.add(s); } }
		 * System.out.println(listOfHeadings);
		 */
	}

}
