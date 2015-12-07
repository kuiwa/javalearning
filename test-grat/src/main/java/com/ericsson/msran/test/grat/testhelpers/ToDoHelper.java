package com.ericsson.msran.test.grat.testhelpers;

import org.apache.log4j.Logger;

/**
 * @name ToDoHelper
 *
 * @author Henrik Djupsjöbacka (etxheda)
 *
 * @created 2015-05-08
 *
 * @description This class handles ToDo methods for test cases that fails.
 *
 * @revision etxheda 2015-05-08 First version.
 *
 */
public class ToDoHelper {

	private Logger logger;

	public ToDoHelper()
	{
		logger = Logger.getLogger(ToDoHelper.class);
	}

	public void todoAssertEquals(int expected, int actual)
	{
		todoAssertEquals("", expected, actual);
	}

	public void todoAssertEquals(String message, int expected, int actual)
	{
		if (expected != actual)
			logger.error("TODO_ASSERT_EQUALS_FAILED " + message);
		else
			logger.error("TODO_ASSERT_EQUALS_PASSED " + message);
	}

	public void todoAssertEquals(Object expected, Object actual)
	{
		todoAssertEquals("", expected, actual);
	}

	public void todoAssertEquals(String message, Object expected, Object actual)
	{
		if (!expected.equals(actual))
			logger.error("TODO_ASSERT_EQUALS_FAILED " + message);
		else
			logger.error("TODO_ASSERT_EQUALS_PASSED " + message);
	}

	public void todoAssertEquals(String expected, String actual)
	{
		todoAssertEquals("", expected, actual);
	}

	public void todoAssertEquals(String message, String expected, String actual)
	{
		if (!expected.equals(actual))
			logger.error("TODO_ASSERT_EQUALS_FAILED " + message);
		else
			logger.error("TODO_ASSERT_EQUALS_PASSED " + message);
	}

	public void todoAssertFalse(Boolean condition)
	{
		todoAssertFalse("", condition);
	}

	public void todoAssertFalse(String message, Boolean condition)
	{
		if (condition)
			logger.error("TODO_ASSERT_FALSE_FAILED " + message);
		else
			logger.error("TODO_ASSERT_FALSE_PASSED " + message);
	}

	public void todoAssertTrue(Boolean condition)
	{
		todoAssertTrue("", condition);
	}

	public void todoAssertTrue(String message, Boolean condition)
	{
		if (!condition)
			logger.error("TODO_ASSERT_TRUE_FAILED " + message);
		else
			logger.error("TODO_ASSERT_TRUE_PASSED " + message);
	}
}
