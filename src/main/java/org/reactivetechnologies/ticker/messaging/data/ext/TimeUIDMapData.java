/**
 * Copyright 2017 esutdal

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.reactivetechnologies.ticker.messaging.data.ext;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.reactivetechnologies.ticker.messaging.data.MapData;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
/**
 * An extension of {@linkplain MapData} with Type 1 UUID (time based) keys.
 * @author esutdal
 *
 * @param <V>
 */
public class TimeUIDMapData<V extends DataSerializable> implements DataSerializable, Map<UUID, V> {

	private MapData<TimeUIDKey, V> map = new MapData<>();
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		map.writeData(out);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		map.readData(in);

	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(new TimeUIDKey((UUID) key));
	}

	@Override
	public boolean containsValue(Object arg0) {
		return map.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<UUID, V>> entrySet() {
		Set<java.util.Map.Entry<UUID, V>> entries = new HashSet<>();
		for(Entry<TimeUIDKey, V> entry : map.entrySet())
		{
			entries.add(new MapDataEntry<UUID, V>(entry.getKey().value(), entry.getValue()));
		}
		return entries;
	}

	@Override
	public V get(Object key) {
		return map.get(new TimeUIDKey((UUID) key));
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<UUID> keySet() {
		Set<UUID> set = new HashSet<>();
		for(TimeUIDKey s : map.keySet())
		{
			set.add(s.value());
		}
		return set;
	}

	@Override
	public V put(UUID arg0, V arg1) {
		return map.put(new TimeUIDKey(arg0), arg1);
	}

	@Override
	public void putAll(Map<? extends UUID, ? extends V> map) {
		for(java.util.Map.Entry<? extends UUID, ? extends V> entry: map.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object arg0) {
		return map.remove(new TimeUIDKey((UUID) arg0));
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

}
