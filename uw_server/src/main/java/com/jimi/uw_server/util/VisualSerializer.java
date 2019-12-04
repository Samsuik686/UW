package com.jimi.uw_server.util;

import com.jfinal.plugin.redis.serializer.ISerializer;

import redis.clients.util.SafeEncoder;

public class VisualSerializer implements ISerializer{

	@Override
	public byte[] keyToBytes(String key) {
		return SafeEncoder.encode(key);
	}

	@Override
	public String keyFromBytes(byte[] bytes) {
		return SafeEncoder.encode(bytes);
	}

	@Override
	public byte[] fieldToBytes(Object field) {
		return SafeEncoder.encode(field.toString());
	}

	@Override
	public Object fieldFromBytes(byte[] bytes) {
		return SafeEncoder.encode(bytes);
	}

	@Override
	public byte[] valueToBytes(Object value) {
		return SafeEncoder.encode(value.toString());
	}

	@Override
	public Object valueFromBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		return SafeEncoder.encode(bytes);
	}

}
