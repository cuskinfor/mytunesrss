/*
 * Copyright (c) 2005 Codewave Software. All Rights Reserved.
 */
package de.codewave.utils;

import java.util.*;

/**
 * Very general utilities.
 */
public class ProgramUtils {
    /**
     * Get a map of parameters from the command line parameters array. This method distinct between options and option values. An options is a
     * parameter starting with a minus sign. The option name is the name without the minus sign. All other command line parameters are option values.
     * Each option name becomes a key in the map. The value of the map entry is an array of strings which are all option values following the option.
     * When an option is followed directly by another option, the value of the map entry will be an empty string array. The first command line
     * parameter has to be an option value.
     * <p/>
     * Example ommand line: <code>[main-class] -file foobar.txt -mode read -colors red green -debug</code>
     * <p/>
     * The map created by this method will contain the following entries: <table> <tr><td>file</td><td>[foobar.txt]</td></tr>
     * <tr><td>mode</td><td>[read]</td></tr> <tr><td>colors</td><td>[red][green]</td></tr> <tr><td>debug</td><td>[]</td></tr> </table>
     * <p/>
     * You can check for the debug flag with the following code for example: <code>resultMap.containsKey("debug")</code>
     * <p/>
     * Or you can get the file name with: <code>resultMap.get("file")</code>
     *
     * @param args The command line parameters or any other string array.
     *
     * @return A map with entries as described above or <code>null</code> in case the first command line argument was not an option.
     */
    public static Map<String, String[]> getCommandLineArguments(String[] args) {
        Map<String, String[]> argumentMap = new HashMap<String, String[]>();
        String currentArgumentName = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                currentArgumentName = args[i].substring(1);
                argumentMap.put(currentArgumentName, new String[0]);
            } else {
                if (currentArgumentName == null) {
                    return null;
                }
                String[] currentAgumentValues = argumentMap.get(currentArgumentName);
                String[] newArgumentValues = new String[currentAgumentValues.length + 1];
                System.arraycopy(currentAgumentValues, 0, newArgumentValues, 0, currentAgumentValues.length);
                newArgumentValues[newArgumentValues.length - 1] = args[i];
                argumentMap.put(currentArgumentName, newArgumentValues);
            }
        }
        return argumentMap;
    }
}