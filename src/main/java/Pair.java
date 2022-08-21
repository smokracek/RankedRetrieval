/*
	Copyright 2021
    Westmont College (c/o Prof. Donald J. Patterson)
 */



import java.io.Serializable;
import java.util.Objects;

/**
 * A simple pair class that keeps to objects together
 * @author djp3, Sam Mokracek
 *
 * @param <K> the type of the first object, aka "the Key"
 * @param <T2> the type of the second object, aka "the Value"
 */
@SuppressWarnings("serial")
public class Pair<K, V> implements Serializable {
	protected K key;
	protected V value;

	public Pair(K k, V v) {
		key = k;
		value = v;
	}

	/**
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(K key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(V value) {
		this.value = value;
	}
	
	/**
	 * @return hashcode for this pair
	 */
	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
	
	/**
	 * @param object object to compare equality to this pair
	 * @return whether this pair equals other passed object (most likely a pair)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Pair<K,V> other = (Pair<K,V>) obj;
		return Objects.equals(key, other.key) && Objects.equals(value, other.value); 
	}
	
	/**
	 * @return string representation of the pair
	 */
	@Override
	public String toString() {
		return "<" + key + ":" + value + ">";
	}
	
}
