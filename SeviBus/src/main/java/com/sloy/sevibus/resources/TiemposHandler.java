package com.sloy.sevibus.resources;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.model.Llegada.Bus;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TiemposHandler extends DefaultHandler {

	Bus tmpBus, bus1, bus2;
	StringBuilder sb = new StringBuilder();
	Builder build = Builder.NO;

	private enum Builder {
		NO, TIEMPO, DISTANCIA, RUTA;
	}

	public Llegada configurarLlegada(Llegada empty) {
		empty.setBus1(bus1);
		empty.setBus2(bus2);
		return empty;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(!Builder.NO.equals(build)){
			StringBuilder sb = new StringBuilder();
			sb.append(ch, start, length);
			switch (build){
				case TIEMPO:
					tmpBus.setTiempo(Integer.parseInt(sb.toString()));
					break;
				case DISTANCIA:
					tmpBus.setDistancia(Integer.parseInt(sb.toString()));
					break;
				default:
					break;
			}
			sb.setLength(0);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(localName.equals("e1")){
			tmpBus = new Bus();
		}else if(localName.equals("e2")){
			tmpBus = new Bus();
		}else if(localName.equals("minutos")){
			build = Builder.TIEMPO;
		}else if(localName.equals("metros")){
			build = Builder.DISTANCIA;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("e1")){
			bus1 = tmpBus;
			tmpBus = null;
		}else if(localName.equals("e2")){
			bus2 = tmpBus;
			tmpBus = null;
		}else if(localName.equals("minutos") || localName.equals("metros")){
			build = Builder.NO;
		}
	}

}
