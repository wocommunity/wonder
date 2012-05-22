//
// TalentPhoto.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// Template created by ishimoto 20110904
//
package webobjectsexamples.businesslogic.eo;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

public class TalentPhoto extends _TalentPhoto {

  /** バージョン ID */
  private static final long serialVersionUID = 1L;

  /** ログ・サポート */
  private static Logger log = Logger.getLogger(TalentPhoto.class);

  //********************************************************************
  //	プロパティ
  //********************************************************************

  //********************************************************************
  //	awake & save
  //********************************************************************

  @Override
  public void awakeFromInsertion(EOEditingContext anEditingContext) {
    if (log.isDebugEnabled())
      log.debug(ENTITY_NAME + " : awakeFromInsertion");

    super.awakeFromInsertion(anEditingContext);

    // ここより書き込む
  }

  //********************************************************************
  //	エンタプライス・オブジェクトのオーバライド
  //********************************************************************

  @Override
  public void updateCreationInformation(EOEditingContext ec) {
  }

  @Override
  public void updateModifyInformation(EOEditingContext ec) {
  }

  @Override
  public void willInsert() {
    super.willInsert();

    // ここから
  }

  @Override
  public void willUpdate() {
    super.willUpdate();

    // ここから
  }

  @Override
  public void mightDelete() {
    super.mightDelete();

    // ここから
  }

  @Override
  public boolean canUpdate() {
    return isUpdateAllowed();
  }

  @Override
  public boolean canDelete() {
    return isDeleteAllowed();
  }

  //********************************************************************
  //	データベース・カスタム・オブジェクト用
  //********************************************************************

  //********************************************************************
  //	検証ロジック
  //********************************************************************

  //********************************************************************
  //	ビジネス・ロジック
  //********************************************************************

}
