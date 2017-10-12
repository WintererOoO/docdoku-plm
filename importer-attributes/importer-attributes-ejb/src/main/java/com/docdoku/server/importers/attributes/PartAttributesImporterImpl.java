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
import org.polarsys.eplmp.server.importers.PartImporter;
import org.polarsys.eplmp.server.importers.PartImporterResult;
import org.polarsys.eplmp.server.importers.PartToImport;

import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that import attribute modification on attribute's part from an Excel File.
 *
 * @author Laurent Le Van
 * @version 1.0.0
 * @since 12/02/16.
 */

@PartAttributesImporter
@Stateless
public class PartAttributesImporterImpl implements PartImporter {

    private static final Logger LOGGER = Logger.getLogger(PartAttributesImporterImpl.class.getName());
    private static final String[] EXTENSIONS = {"xls"};
    private static final String I18N_CONF = "/com/docdoku/server/importers/attributes/ExcelImport";

    private Properties properties;

    @Override
    public boolean canImportFile(String importFileName) {
        String ext = FileIO.getExtension(importFileName);
        return Arrays.asList(EXTENSIONS).contains(ext);
    }

    @Override
    public PartImporterResult importFile(Locale locale, String workspaceId, File file, boolean autoCheckout, boolean autoCheckIn, boolean permissiveUpdate) {

        properties = PropertiesLoader.loadLocalizedProperties(locale, I18N_CONF, PartAttributesImporterImpl.class);

        Map<String, PartToImport> partsToImport = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            ExcelParser excelParser = new ExcelParser(file, locale);
            List<String> checkFileErrors = excelParser.checkFile();
            errors.addAll(checkFileErrors);
            if (errors.isEmpty()) {
                partsToImport = excelParser.getPartsToImport();
            }
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
            return new PartImporterResult(file, warnings, errors, null, null, null);
        }

        return new PartImporterResult(file, warnings, errors, null, null, partsToImport);
    }
/*
        List<PartToImport> listParts = new ArrayList<>();

        for (PartToImport part : partsToImport.values()) {

            try {
                PartMaster currentPartMaster = productManager.getPartMaster(new PartMasterKey(workspaceId, part.getNumber()));

                PartIteration partIteration = currentPartMaster.getLastRevision().getLastIteration();

                boolean hasAccess = productManager.canWrite(currentPartMaster.getLastRevision().getKey());

                if (!part.getAttributes().isEmpty() && (hasAccess && canChangePart(workspaceId, partIteration.getPartRevision(), autoCheckout))) {

                    //info : we create 2 instanceAttribute Lists to ensure separation between current list and updated list
                    List<InstanceAttribute> updatedInstanceAttributes = AttributesImporterUtils.getInstanceAttributes(properties, partIteration.getInstanceAttributes(), errors);//we will update data here
                    List<InstanceAttribute> currentInstanceAttributes = new ArrayList<>(updatedInstanceAttributes);//we will delete updated attributes from here

                    List<Attribute> attributes = part.getAttributes();
                    part.getNumber();
                    AttributesImporterUtils.updateAndCreateInstanceAttributes(lovManager, properties, attributes, currentInstanceAttributes, part.getNumber(), errors, workspaceId, updatedInstanceAttributes);
                    part.setInstanceAttributes(updatedInstanceAttributes);
                    if (revisionNote != null && !revisionNote.isEmpty()) {
                        part.setRevisionNote(revisionNote);
                    }
                    part.setPartIteration(partIteration);
                    listParts.add(part);

                } else if (permissiveUpdate && !hasAccess) {
                    warnings.add(AttributesImporterUtils.createError(properties, "NotAccess", part.getNumber()));
                    LOGGER.log(Level.WARNING, "No right on [" + part.getNumber() + "]");

                } else if (!canChangePart(workspaceId, partIteration.getPartRevision(), autoCheckout)) {
                    User user = userManager.checkWorkspaceReadAccess(workspaceId);

                    if (partIteration.getPartRevision().isCheckedOut() && !partIteration.getPartRevision().getCheckOutUser().equals(user)) {
                        String errorMessage = AttributesImporterUtils.createError(properties, "AlreadyCheckedOut", part.getNumber(), partIteration.getPartRevision().getCheckOutUser().getName());
                        if (permissiveUpdate) {
                            warnings.add(errorMessage);
                        } else {
                            errors.add(errorMessage);
                        }

                    } else if (!partIteration.getPartRevision().isCheckedOut()) {
                        String errorMessage = AttributesImporterUtils.createError(properties, "NotCheckedOut", part.getNumber());
                        if (permissiveUpdate) {
                            warnings.add(errorMessage);
                        } else {
                            errors.add(errorMessage);
                        }
                    }
                }

            } catch
                    (AccessRightException | UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException
                            | PartMasterNotFoundException | PartRevisionNotFoundException | WorkspaceNotEnabledException e) {
                LOGGER.log(Level.WARNING, "Could not get PartMaster[" + part.getNumber() + "]", e);
                errors.add(e.getLocalizedMessage());
            }
        }

        if (errors.size() > 0) {
            return new ImportResult(file, warnings, errors);
        }

        try {
            bulkPartUpdate(listParts, workspaceId, autoCheckout, autoCheckin, permissiveUpdate, errors, warnings);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
            errors.add("Unhandled exception");
        }
        return new ImportResult(file, warnings, errors);
    }

    @Override
    public ImportPreview dryRunImport(Locale locale, String workspaceId, File file, String originalFileName, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate) {

        properties = PropertiesLoader.loadLocalizedProperties(locale, I18N_CONF, PartAttributesImporterImpl.class);

        List<PartRevision> toCheckout = new ArrayList<>();

        Map<String, PartToImport> partsInFile = new HashMap<>();

        try {
            ExcelParser excelParser = new ExcelParser(file, locale);
            List<String> errors = excelParser.checkFile();
            partsInFile = excelParser.getPartsToImport();

            if (!errors.isEmpty()) {
                return null;
            }

        } catch (IOException | InvalidFormatException | WrongCellCommentException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        for (PartToImport part : partsInFile.values()) {
            try {
                PartRevision currentPartRevision = productManager.getPartMaster(new PartMasterKey(workspaceId, part.getNumber())).getLastRevision();
                PartIteration currentPartIteration = currentPartRevision.getLastIteration();

                if (autoCheckout && !currentPartRevision.isCheckedOut() && productManager.canWrite(currentPartRevision.getKey()) && !part.getAttributes().isEmpty()
                        && AttributesImporterUtils.checkIfUpdateOrCreateInstanceAttributes(part.getAttributes(), currentPartIteration.getInstanceAttributes())) {
                    toCheckout.add(currentPartRevision);
                }
            } catch (UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException
                    | PartMasterNotFoundException | PartRevisionNotFoundException | AccessRightException
                    | WorkspaceNotEnabledException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }

        return new ImportPreview(toCheckout, new ArrayList<>());

    }




    private boolean canChangePart(String workspaceId, PartRevision lastRevision, boolean autoCheckout)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return (autoCheckout && !lastRevision.isCheckedOut()) || (lastRevision.isCheckedOut() && lastRevision.getCheckOutUser().equals(user));
    }

    public void bulkPartUpdate(List<PartToImport> parts, String workspaceId, boolean autoCheckout, boolean autoCheckin, boolean permissive, List<String> errors, List<String> warnings) throws Exception {

        LOGGER.log(Level.INFO, "Bulk parts update");
        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        boolean errorOccured = false;
        Exception exception = null;

        for (PartToImport part : parts) {
            PartIteration partIteration = part.getPartIteration();

            try {
                PartMaster currentPartMaster = productManager.getPartMaster(new PartMasterKey(workspaceId, part.getNumber()));

                boolean isAutoCheckedOutByImport = false; //to check if checkout for the update

                if (autoCheckout && !currentPartMaster.getLastRevision().isCheckedOut() && productManager.canWrite(currentPartMaster.getLastRevision().getKey())) {
                    PartRevision currentPartRevision = productManager.checkOutPart(new PartRevisionKey(workspaceId, part.getNumber(), currentPartMaster.getLastRevision().getVersion()));
                    isAutoCheckedOutByImport = true;
                    partIteration = currentPartRevision.getLastIteration();
                }

                //Check if not permissive or permissive and checked out
                if (partIteration.getPartRevision().isCheckedOut() && partIteration.getPartRevision().getCheckOutUser().equals(user)) {
                    //Do not lose previous saved revision note if no note specified during import
                    if (part.getRevisionNote() == null) {
                        part.setRevisionNote(partIteration.getIterationNote());
                    }
                    productManager.updatePartIteration(partIteration.getKey(), part.getRevisionNote(), partIteration.getSource(), null, part.getInstanceAttributes(), null, null, null, null);
                } else {
                    throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25", partIteration.getPartRevision().toString());
                }

                //CheckIn if checkout before
                if (autoCheckin && isAutoCheckedOutByImport && partIteration.getPartRevision().getCheckOutUser().equals(user)) {
                    try {
                        productManager.checkInPart(new PartRevisionKey(currentPartMaster.getKey(), currentPartMaster.getLastRevision().getVersion()));
                    } catch (NotAllowedException e) {
                        LOGGER.log(Level.WARNING, null, e);
                        warnings.add(e.getLocalizedMessage());
                    }
                }
            } catch (CreationException | PartMasterNotFoundException | EntityConstraintException | UserNotFoundException | WorkspaceNotFoundException | UserNotActiveException | PartUsageLinkNotFoundException | PartRevisionNotFoundException | AccessRightException | FileAlreadyExistsException e) {
                LOGGER.log(Level.WARNING, null, e);
                errors.add(e.getLocalizedMessage() + ": " + partIteration.getNumber());
                errorOccured = true;
                exception = e;

            } catch (NotAllowedException e) {
                LOGGER.log(Level.WARNING, null, e);
                if (permissive) {
                    warnings.add(e.getLocalizedMessage());
                } else {
                    errors.add(e.getLocalizedMessage());
                    errorOccured = true;
                    exception = e;
                }
            }
        }

        LOGGER.log(Level.INFO, "Bulk parts update finished");

        if (errorOccured) {
            throw exception;
        }
    }  */
}

