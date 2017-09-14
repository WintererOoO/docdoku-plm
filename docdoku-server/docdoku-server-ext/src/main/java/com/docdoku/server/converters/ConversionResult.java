/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.converters;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This ConversionResult class represents the conversion status done by a
 * CADConverter plugin.
 * <p>
 * It holds the converted file and its materials.
 */
public class ConversionResult implements Closeable, Serializable {

    public static class Position implements Serializable {

	/**
	 * Position of a component instance
	 */
	private static final long serialVersionUID = 1L;

	private double[] translation;
	private double[][] rotationmatrix;

	/**
	 * Constructor for a component position.
	 * 
	 * @param x
	 *            1st column of the rotation matrix
	 * @param y
	 *            2nd column of the rotation matrix
	 * @param z
	 *            3rd column of the rotation matrix
	 * @param o
	 *            Translation vector
	 */
	public Position(double[][] rm, double[] o) {
	    this.translation = o;
	    this.rotationmatrix = rm;
	}

	public double[] getTranslation() {
	    return translation;
	}

	public double[][] getRotationMatrix() {
	    return this.rotationmatrix;
	}
    }
    
    private static final Logger LOGGER = Logger.getLogger(ConversionResult.class.getName());

    private static final long serialVersionUID = 1L;

    /**
     * The converted file for succeed conversions
     */
    private URI convertedFile;
    /**
     * The list of materials files if any
     */
    private List<URI> materials = new ArrayList<>();
    /**
     * The output of conversion program
     */
    private String stdOutput;
    /**
     * The error output of conversion program
     */
    private String errorOutput;

    private Map<String, List<Position>> componentPositionMap;

    /**
     * Default constructor
     */
    public ConversionResult() {
    }

    /**
     * Constructor with converted file
     *
     * @param convertedFile
     *            the converted file
     */
    public ConversionResult(Path convertedFile) {
	this.convertedFile = convertedFile.toUri();
    }

    /**
     * Constructor with converted file and materials
     *
     * @param convertedFile
     *            the converted file
     */
    public ConversionResult(Path convertedFile, List<Path> materials) {
	this.convertedFile = convertedFile.toUri();
	this.materials = new ArrayList<>();
	materials.forEach((path) -> this.materials.add(path.toUri()));
    }

    /**
     * Constructor with assembly component-position map.
     * 
     * @param componentPositionMap
     *            Assembly components and positions
     */
    public ConversionResult(Map<String, List<Position>> componentPositionMap) {
	this.componentPositionMap = componentPositionMap;
    }

    public Path getConvertedFile() {
	return convertedFile != null ? Paths.get(convertedFile) : null;
    }

    void setConvertedFile(Path convertedFile) {
	this.convertedFile = convertedFile.toUri();
    }

    public List<Path> getMaterials() {
	return materials.stream().map((uri) -> {
	    return Paths.get(uri);
	}).collect(Collectors.toList());
    }

    void setMaterials(List<Path> materials) {
	this.materials = new ArrayList<>();
	materials.forEach((path) -> this.materials.add(path.toUri()));
    }

    public String getStdOutput() {
	return stdOutput;
    }

    public void setStdOutput(String stdOutput) {
	this.stdOutput = stdOutput;
    }

    public String getErrorOutput() {
	return errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
	this.errorOutput = errorOutput;
    }

    public Map<String, List<Position>> getComponentPositionMap() {
	return this.componentPositionMap;
    }

    void setComponentPositionMap(Map<String, List<Position>> componentPositionMap) {
	this.componentPositionMap = componentPositionMap;
    }

    public void close() {
	try {
	    if (convertedFile != null) {
		Files.deleteIfExists(Paths.get(convertedFile));
	    }
	    if (materials != null) {
		for (URI m : materials) {
		    Files.deleteIfExists(Paths.get(m));
		}
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, null, e);
	}
    }
}
