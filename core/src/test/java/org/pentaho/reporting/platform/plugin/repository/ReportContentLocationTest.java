/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.reporting.platform.plugin.repository;

import org.junit.*;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.repository.ContentCreationException;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.ContentIOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReportContentLocationTest {
  ReportContentRepository reportContentRepository;
  RepositoryFile repositoryFile;
  ReportContentLocation reportContentLocation;
  IUnifiedRepository repository;

  @AfterClass
  public static void restoreBackup() {
    // Remove mocked references used in PentahoSystem.
    PentahoSystem.shutdown();
  }

  @Before
  public void setUp() throws Exception {
    PentahoSystem.shutdown();
    repositoryFile = mock( RepositoryFile.class );
    doReturn( "contentId" ).when( repositoryFile ).getId();
    doReturn( "contentName" ).when( repositoryFile ).getName();
    doReturn( "version" ).when( repositoryFile ).getVersionId();
    reportContentRepository = mock( ReportContentRepository.class );
    reportContentLocation = new ReportContentLocation( repositoryFile, reportContentRepository );
    repository = mock( IUnifiedRepository.class );
    PentahoSystem.registerObject( repository, IUnifiedRepository.class );
  }

  @Test
  public void testIsHiddenExtension() throws Exception {
    assertTrue( reportContentLocation.isHiddenExtension( ".jpe" ) );
    assertTrue( reportContentLocation.isHiddenExtension( ".jpeg" ) );
    assertTrue( reportContentLocation.isHiddenExtension( ".jpg" ) );
    assertTrue( reportContentLocation.isHiddenExtension( ".png" ) );
    assertTrue( reportContentLocation.isHiddenExtension( ".css" ) );
    assertFalse( reportContentLocation.isHiddenExtension( "" ) );
  }

  @Test
  public void testDelete() throws Exception {
    assertFalse( reportContentLocation.delete() );
  }

  @Test
  public void testGetParent() throws Exception {
    assertNull( reportContentLocation.getParent() );
  }

  @Test
  public void testGetRepository() throws Exception {
    assertEquals( reportContentRepository, reportContentLocation.getRepository() );
  }

  @Test
  public void testSetAttribute() throws Exception {
    assertFalse( reportContentLocation.setAttribute( "", "", null ) );
  }

  @Test
  public void testCreateLocation() throws Exception {
    try {
      reportContentLocation.createLocation( "" );
    } catch ( ContentCreationException ex ) {
      assertTrue( true );
    }
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals( "contentName", reportContentLocation.getName() );
  }

  @Test
  public void testGetContentId() throws Exception {
    assertEquals( "contentId", reportContentLocation.getContentId() );
  }

  @Test
  public void testGetAttribute() throws Exception {
    assertEquals( null, reportContentLocation.getAttribute( "", "" ) );
    assertEquals( null, reportContentLocation.getAttribute( "org.jfree.repository", "" ) );
    assertEquals( null, reportContentLocation.getAttribute( "", "version" ) );
    assertEquals( "version", reportContentLocation.getAttribute( "org.jfree.repository", "version" ) );
  }

  @Test( expected = NullPointerException.class )
  public void testNullRepo() {
    new ReportContentLocation( mock( RepositoryFile.class ), null );
  }

  @Test( expected = NullPointerException.class )
  public void testNullLocation() {
    new ReportContentLocation( null, mock( ReportContentRepository.class ) );
  }


  @Test
  public void testList() throws ContentIOException {


    final ArrayList<RepositoryFile> repositoryFiles = new ArrayList<>();
    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    final String value = UUID.randomUUID().toString();
    when( repositoryFile.getName() ).thenReturn( value );
    repositoryFiles.add( repositoryFile );
    when( repository.getChildren( any( Serializable.class ) ) ).thenReturn( repositoryFiles );

    final ReportContentLocation reportContentLocation =
      new ReportContentLocation( this.repositoryFile, reportContentRepository );

    final ContentEntity[] contentEntities = reportContentLocation.listContents();

    assertNotNull( contentEntities );
    assertEquals( 1, contentEntities.length );
    assertEquals( value, contentEntities[ 0 ].getName() );
  }


  @Test( expected = ContentIOException.class )
  public void testGetEntryNotExist() throws ContentIOException {

    final ReportContentLocation reportContentLocation =
      new ReportContentLocation( this.repositoryFile, reportContentRepository );

    reportContentLocation.getEntry( "test" );
  }

  @Test
  public void testGetEntry() throws ContentIOException {


    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    final String value = UUID.randomUUID().toString();
    when( repositoryFile.getName() ).thenReturn( value );

    when( repository.getFile( "null/test" ) ).thenReturn( repositoryFile );

    final ReportContentLocation reportContentLocation =
      new ReportContentLocation( this.repositoryFile, reportContentRepository );

    final ContentEntity test = reportContentLocation.getEntry( "test" );

    assertNotNull( test );
    assertEquals( value, test.getName() );

  }

  @Test
  public void testCreateItem() throws Exception {
    final HashMap<String, Serializable> metadata = new HashMap<>();
    when( repository.getFile( anyString() ) ).thenReturn( repositoryFile );
    when( repository.getFileMetadata( any() ) ).thenReturn( metadata );
    when( repositoryFile.getPath() ).thenReturn( "/testPath" );
    reportContentLocation.createItem( "testName" );
    Assert.assertTrue( repository.getFileMetadata( repositoryFile.getId() ).containsKey( ReportContentLocation.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

}

