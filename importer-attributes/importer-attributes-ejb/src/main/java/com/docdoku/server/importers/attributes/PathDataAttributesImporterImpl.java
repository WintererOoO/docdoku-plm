/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server.importers.attributes;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.polarsys.eplmp.core.util.FileIO;
import org.polarsys.eplmp.i18n.PropertiesLoader;
import org.polarsys.eplmp.server.importers.AttributesImporterUtils;
import org.polarsys.eplmp.server.importers.PathDataImporter;
import org.polarsys.eplmp.server.importers.PathDataImporterResult;
import org.polarsys.eplmp.server.importers.PathDataToImport;

import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that import attribute modification on attribute's Path Data from an Excel File.
 *
 * @author Laurent Le Van
 * @version 1.0.0
 * @since 12/02/16.
 */

@PathDataAttributesImporter
@Stateless
public class PathDataAttributesImporterImpl implements PathDataImporter {

    private static final String[] EXTENSIONS = {"xls"};
    private static final Logger LOGGER = Logger.getLogger(PathDataAttributesImporterImpl.class.getName());

    private static final String I18N_CONF = "/com/docdoku/server/importers/attributes/ExcelImport";

    private Properties properties;

    /**
     * Check if valid extension
     *
     * @param importFileName name of the file we want to import
     * @return true if good extension, false else
     */
    @Override
    public boolean canImportFile(String importFileName) {
        String ext = FileIO.getExtension(importFileName);
        return Arrays.asList(EXTENSIONS).contains(ext);
    }

    /**
     * This method import data of a file with different options
     *
     * @param workspaceId      Workspace in which we work
     * @param file             file containing data we want to update
     * @param autoFreeze       autofreeze after modification
     * @param permissiveUpdate boolean to indicate if allow or not permissive update
     * @return an ImportResult object containing file, and warnings, errors
     */
    @Override
    public PathDataImporterResult importFile(Locale locale, String workspaceId, File file, boolean autoFreeze, boolean permissiveUpdate) {

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        properties = PropertiesLoader.loadLocalizedProperties(locale, I18N_CONF, PartAttributesImporterImpl.class);

        Map<String, PathDataToImport> result = new HashMap<>();

        try {
            ExcelParser excelParser = new ExcelParser(file, locale);
            List<String> checkFileErrors = excelParser.checkFile();
            errors.addAll(checkFileErrors);
            result = excelParser.importPathData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            errors.add(AttributesImporterUtils.createError(properties, "InternalError", "IOException"));
        } catch (InvalidFormatException e) {
            LOGGER.log(Level.SEVERE, null, e);
            errors.add(AttributesImporterUtils.createError(properties, "InvalidFormatException"));
        } catch (WrongCellCommentException e) {
            errors.add(AttributesImporterUtils.createError(properties, "WrongCellCommentException"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            errors.add(AttributesImporterUtils.createError(properties, "InternalError", e.toString()));
        }

        if (!errors.isEmpty()) {
            return new PathDataImporterResult(file, warnings, errors, null, null, null);
        }

        return new PathDataImporterResult(file, warnings, errors, null, null, result);
/*
        List<PathDataToImport> listPathData = new ArrayList<>();

        LOGGER.log(Level.INFO, "Iterate  pathData ... ");

        // will check access rights only once for each instances
        Map<String, Map<String, Boolean>> instancesAccess = new HashMap<>();
        Map<String, Map<String, ProductInstanceIteration>> productInstancesCache = new HashMap<>();

        for (PathDataToImport pathData : result.values()) {
            createOrUpdatePathData(workspaceId, pathData, permissiveUpdate, revisionNote, listPathData, errors, instancesAccess, productInstancesCache);
        }
        LOGGER.log(Level.INFO, "Iterate  pathData finished");


        if (errors.size() > 0) {
            return new ImportResult(file, warnings, errors);
        }

        try {
            bulkPathDataUpdate(listPathData, workspaceId, autoFreeze, permissiveUpdate, errors, warnings);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
            errors.add("Unhandled exception");
        }
        return new ImportResult(file, warnings, errors);*/
    }
/*
    private void createOrUpdatePathData(String workspaceId, PathDataToImport pathData, boolean permissiveUpdate, String revisionNote, List<PathDataToImport> listPathData, List<String> errors, Map<String, Map<String, Boolean>> instancesAccess, Map<String, Map<String, ProductInstanceIteration>> productInstancesCache) {

        try {

            // Cache hack start //
            String productId = pathData.getProductId();
            String serialNUmber = pathData.getSerialNumber();
            Map<String, Boolean> productAccess = instancesAccess.get(productId);

            if (productAccess == null) {
                productAccess = new HashMap<>();
                instancesAccess.put(productId, productAccess);
            }

            Boolean hasInstanceAccess = productAccess.get(serialNUmber);
            if (hasInstanceAccess == null) {
                hasInstanceAccess = productInstanceManager.canWrite(workspaceId, pathData.getProductId(), pathData.getSerialNumber());
                productAccess.put(serialNUmber, hasInstanceAccess);
            }

            ProductInstanceIteration productInstanceIteration = null;
            if (hasInstanceAccess) {
                Map<String, ProductInstanceIteration> cache = productInstancesCache.get(productId);
                if (cache == null) {
                    cache = new HashMap<>();
                    productInstancesCache.put(productId, cache);
                }

                productInstanceIteration = cache.get(serialNUmber);

                if (productInstanceIteration == null) {
                    ProductInstanceMaster productInstanceMaster = productInstanceManager.getProductInstanceMaster(new ProductInstanceMasterKey(serialNUmber, workspaceId, productId));
                    productInstanceIteration = productInstanceMaster.getLastIteration();
                    cache.put(serialNUmber, productInstanceIteration);

                }
            }

            // Cache hack end //

            if (!pathData.getAttributes().isEmpty() && (!permissiveUpdate || (permissiveUpdate && hasInstanceAccess))) {

                // 2 Possibilities : PathDataMasterId null => create new Path Data, not null => update PathData
                PathDataMaster currentPathDataMaster = findPathDataMaster(productInstanceIteration, pathData.getPath());

                if (currentPathDataMaster != null) {
                    PathDataIteration pathDataIteration = currentPathDataMaster.getLastIteration();

                    //info : we create 2 instanceAttribute Lists to ensure separation between current list and updated list
                    List<InstanceAttribute> updatedInstanceAttributes = AttributesImporterUtils.getInstanceAttributes(properties, pathDataIteration.getInstanceAttributes(), errors);//we will update data here
                    List<InstanceAttribute> currentInstanceAttributes = new ArrayList<>(updatedInstanceAttributes);//we will delete updated attributes from here

                    List<Attribute> attributes = pathData.getAttributes();
                    AttributesImporterUtils.updateAndCreateInstanceAttributes(lovManager, properties, attributes, currentInstanceAttributes, pathData.getPath(), errors, workspaceId, updatedInstanceAttributes);
                    pathData.setInstanceAttributes(updatedInstanceAttributes);
                    pathData.setRevisionNote(revisionNote);
                    pathData.setPathDataIteration(pathDataIteration);
                    listPathData.add(pathData);
                } else {
                    List<Attribute> attributes = pathData.getAttributes();

                    List<InstanceAttribute> newInstanceAttributes = new ArrayList<>();

                    for (Attribute attribute : attributes) {
                        InstanceAttribute instanceAttribute = AttributesImporterUtils.createAttribute(lovManager, properties, attribute, workspaceId, errors);
                        newInstanceAttributes.add(instanceAttribute);
                    }
                    pathData.setPath(pathData.getPath());
                    pathData.setInstanceAttributes(newInstanceAttributes);
                    pathData.setRevisionNote(revisionNote);
                    listPathData.add(pathData);
                }

            }
        } catch (UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException | WorkspaceNotEnabledException e) {
            LOGGER.log(Level.WARNING, "Could not get PathData Master [" + pathData.getPath() + "]", e);
            errors.add(e.getLocalizedMessage());
        } catch (ProductInstanceMasterNotFoundException e) {
            LOGGER.log(Level.WARNING, "Could not get Product Instance Master [" + pathData.getPath() + "]", e);
            errors.add(e.getLocalizedMessage());
        }
    }

    private PathDataMaster findPathDataMaster(ProductInstanceIteration productInstanceIteration, String path) {
        for (PathDataMaster pathDataMaster : productInstanceIteration.getPathDataMasterList()) {
            if (pathDataMaster.getPath().equals(path)) {
                return pathDataMaster;
            }
        }
        return null;
    }



    public void bulkPathDataUpdate(List<PathDataToImport> pathDataList, String workspaceId, boolean autoFreeze, boolean permissive, List<String> errors, List<String> warnings) throws Exception {

        LOGGER.log(Level.INFO, "Bulk path data update");

        boolean errorOccured = false;
        Exception exception = null;


        for (PathDataToImport pathData : pathDataList) {
            try {

                PathDataMaster currentPathDataMaster = productInstanceManager.getPathDataByPath(workspaceId, pathData.getProductId(), pathData.getSerialNumber(), pathData.getPath());

                PathDataMaster pathDataMaster;

                if (currentPathDataMaster != null) {
                    pathDataMaster = productInstanceManager.addNewPathDataIteration(workspaceId, pathData.getProductId(), pathData.getSerialNumber(), currentPathDataMaster.getId(), cloneAttributes(pathData.getInstanceAttributes()), pathData.getRevisionNote(), null, null);
                } else {
                    pathDataMaster = productInstanceManager.createPathDataMaster(workspaceId, pathData.getProductId(), pathData.getSerialNumber(), pathData.getPath(), cloneAttributes(pathData.getInstanceAttributes()), pathData.getRevisionNote());
                }
                if (autoFreeze) {
                    productInstanceManager.addNewPathDataIteration(workspaceId, pathData.getProductId(), pathData.getSerialNumber(), pathDataMaster.getId(), cloneAttributes(pathDataMaster.getLastIteration().getInstanceAttributes()), null, null, null);
                }

            } catch (UserNotFoundException | WorkspaceNotFoundException | UserNotActiveException | AccessRightException | ProductInstanceMasterNotFoundException | PathDataAlreadyExistsException e) {
                LOGGER.log(Level.SEVERE, null, e);
                errors.add(e.getLocalizedMessage());
                errorOccured = true;
                exception = e;

            } catch (NotAllowedException e) {
                if (permissive) {
                    warnings.add(e.getLocalizedMessage());
                } else {
                    errors.add(e.getLocalizedMessage());
                    errorOccured = true;
                    exception = e;
                }
            }

        }

        LOGGER.log(Level.INFO, "Bulk path data update finished");

        if (errorOccured) {
            throw exception;
        }
    }

    private List<InstanceAttribute> cloneAttributes(List<InstanceAttribute> pAttributes) {
        List<InstanceAttribute> attributes = new ArrayList<>();
        for (InstanceAttribute instanceAttribute : pAttributes) {
            attributes.add(instanceAttribute.clone());
        }
        return attributes;
    }
    */

}

