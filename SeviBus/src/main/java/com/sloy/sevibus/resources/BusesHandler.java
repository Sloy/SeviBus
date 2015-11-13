package com.sloy.sevibus.resources;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BusesHandler extends DefaultHandler {

	BusLocation tmpBus;
	StringBuilder sb = new StringBuilder();
	Builder build = Builder.NO;
	List<BusLocation> buses = new ArrayList<BusLocation>();

	private enum Builder {
		NO, X, Y;
	}
	
	public List<BusLocation> getBuses(){
		return buses;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(!Builder.NO.equals(build)){
			StringBuilder sb = new StringBuilder();
			sb.append(ch, start, length);
			switch (build){
				case X:
					tmpBus.xcoord = Integer.parseInt(sb.toString());
					break;
				case Y:
					tmpBus.ycoord = Integer.parseInt(sb.toString());
					break;
				default:
					break;
			}
			sb.setLength(0);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(localName.equals("InfoVehiculo")){
			tmpBus = new BusLocation();
		}else if(localName.equals("xcoord")){
			build = Builder.X;
		}else if(localName.equals("ycoord")){
			build = Builder.Y;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("InfoVehiculo")){
			buses.add(tmpBus);
			tmpBus = null;
		}else if(localName.equals("xcoord") || localName.equals("ycoord")){
			build = Builder.NO;
		}
	}

}
