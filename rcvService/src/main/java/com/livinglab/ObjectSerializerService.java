package com.livinglab;

import java.io.Serializable;

public interface ObjectSerializerService {

	public String objectToString(Serializable object);
	public <t extends Serializable>t stringToObject(String string);
}
