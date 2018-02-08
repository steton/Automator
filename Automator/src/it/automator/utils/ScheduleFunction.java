package it.automator.utils;

@FunctionalInterface
public interface ScheduleFunction {
	public void apply() throws Exception;
}
