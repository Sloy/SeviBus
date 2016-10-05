package com.sloy.sevibus.resources;

import com.sloy.sevibus.model.ArrivalTime;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TiemposHandler extends DefaultHandler {

	Bus tmpBus, bus1, bus2;
	Builder build = Builder.NO;

	private enum Builder {
		NO, TIEMPO, DISTANCIA, RUTA;
	}

	public ArrivalTime configurarLlegada(ArrivalTime empty) {
		empty.setNextBus(busArrivalFromBus(bus1));
		empty.setSecondBus(busArrivalFromBus(bus2));
		return empty;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(!Builder.NO.equals(build)){
			StringBuilder sb = new StringBuilder();
			sb.append(ch, start, length);
			switch (build){
				case TIEMPO:
					tmpBus.time = (Integer.parseInt(sb.toString()));
					break;
				case DISTANCIA:
					tmpBus.distance = (Integer.parseInt(sb.toString()));
					break;
				default:
					break;
			}
			sb.setLength(0);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if("e1".equals(localName)){
			tmpBus = new Bus();
		}else if("e2".equals(localName)){
			tmpBus = new Bus();
		}else if("minutos".equals(localName)){
			build = Builder.TIEMPO;
		}else if("metros".equals(localName)){
			build = Builder.DISTANCIA;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if("e1".equals(localName)){
			bus1 = tmpBus;
			tmpBus = null;
		}else if("e2".equals(localName)){
			bus2 = tmpBus;
			tmpBus = null;
		}else if("minutos".equals(localName) || "metros".equals(localName)){
			build = Builder.NO;
		}
	}

	private ArrivalTime.BusArrival busArrivalFromBus(Bus bus) {
		ArrivalTime.Status status = statusForBus(bus);
		ArrivalTime.BusArrival busArrival = new ArrivalTime.BusArrival(status);
		if (bus != null) {
			busArrival.setTimeInMinutes(bus.time);
			busArrival.setDistanceInMeters(bus.distance);
		}
		return busArrival;
	}

	private ArrivalTime.Status statusForBus(Bus bus) {
		if (bus == null) {
			return ArrivalTime.Status.NOT_AVAILABLE;
		} else {
			if (bus.time > 0) {
				return ArrivalTime.Status.ESTIMATE;
			} else if (bus.time == 0) {
				return ArrivalTime.Status.IMMINENT;
			} else {
				return ArrivalTime.Status.NO_ESTIMATION;
			}
		}
	}

	class Bus {
		int time;
		int distance;
	}

}
