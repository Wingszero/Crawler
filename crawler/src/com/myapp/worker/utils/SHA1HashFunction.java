package com.myapp.worker.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class SHA1HashFunction
{
	private BigInteger hash_unit;

	public SHA1HashFunction(int totalWorkers) 
	{
		init(totalWorkers);
	}

	public void init(int n)
	{
		String sha1_max_str = "";
		//hex is 4 bits * 40 = 160 bits(SHA1)
		for(int i = 0; i < 40 ; ++i)
		{
			sha1_max_str += "F";
		}
		BigInteger SHA1_MAX = new BigInteger(sha1_max_str, 16).add(new BigInteger("1"));
		BigInteger big_n = new BigInteger(String.valueOf(n));
		//split the hash space into n workers
		hash_unit = SHA1_MAX.divide(big_n);
	}

	/**
	 * determine a key belongs to which hash space index
	 * the hash space is a SHA1, has 160 bits, which is 40 hex
	 * @return the hash index 
	 */
	public int getHashIndex(String key) 
	{
		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("SHA1");
		}
		catch(NoSuchAlgorithmException e)
		{
			
		}

		String hex_string = DatatypeConverter.printHexBinary(md.digest(key.getBytes()));

		BigInteger bi_key = new BigInteger(hex_string, 16);

		BigInteger bi_idx = bi_key.divide(hash_unit);

		//print("dispatchKeyToWorker i: " + i + " " + key + " " + value);
		return bi_idx.intValue();
	}
}
