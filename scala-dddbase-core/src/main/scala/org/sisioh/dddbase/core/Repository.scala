/*
 * Copyright 2010 TRICREO, Inc. (http://tricreo.jp/)
 * Copyright 2011 Sisioh Project and others. (http://www.sisioh.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.sisioh.dddbase.core

import util.Try

/**
 * [[org.sisioh.dddbase.core.Identity]]を用いて、[[org.sisioh.dddbase.core.Entity]]
 * を検索する責務を表すインターフェイス。
 *
 * @author j5ik2o
 */
trait EntityResolver[ID, T <: Entity[ID]] {

  /**
   * 識別子に該当するエンティティを取得する。
   *
   * @param identifier 識別子
   * @return Try[E]
   *
   * @throws IllegalArgumentException
   */
  def resolve(identifier: Identity[ID]): Try[T]

  def resolveOption(identifier: Identity[ID]): Option[T]

  def apply(identifier: Identity[ID]) = resolve(identifier)

  /**
   * 指定した識別子のエンティティが存在するかを返す。
   *
   * @param identifier 識別子
   * @return 存在する場合はtrue
   * @throws RepositoryException リポジトリにアクセスできない場合
   */
  def contains(identifier: Identity[ID]): Boolean

  /**
   * 指定したのエンティティが存在するかを返す。
   *
   * @param entity エンティティ
   * @return 存在する場合はtrue
   * @throws RepositoryException リポジトリにアクセスできない場合
   */
  def contains(entity: T): Boolean = contains(entity.identity)

}

/**
 * [[scala.collection.Iterable]]
 */
trait EntityIterableResolver[ID, T <: Entity[ID]] extends Iterable[T] {
  this: EntityResolver[ID, T] =>

  def contains(identifier: Identity[ID]): Boolean = exists(_.identity == identifier)

}

/**
 * 基本的なリポジトリのトレイト。
 * リポジトリとして、基本的に必要な機能を定義するトレイト。
 *
 * @tparam T エンティティの型
 * @tparam ID エンティティの識別子の型
 *
 * @author j5ik2o
 */
trait Repository[ID, T <: Entity[ID]] extends EntityResolver[ID, T] {

  /**
   * エンティティを保存する。
   *
   * @param entity 保存する対象のエンティティ
   * @return Try
   */
  def store(entity: T): Try[Repository[ID, T]]

  def update(identifier: Identity[ID], entity: T) = store(entity)

  /**
   * 指定した識別子のエンティティを削除する。
   *
   * @param identity 識別子
   * @return Try
   */
  def delete(identity: Identity[ID]): Try[Repository[ID, T]]

  /**
   * 指定したエンティティを削除する。
   *
   * @param entity エンティティ
   * @return Try
   */
  def delete(entity: T): Try[Repository[ID, T]] = delete(entity.identity)

}

trait CallbackEntityResolver[ID, T <: Entity[ID]] {
  this: EntityResolver[ID, T] =>

  def resolve[R](callbak: T => R): R

}

/**
 * ページングによる検索を行うためのトレイト。
 *
 * @author j5ik2o
 */
trait PagingEntityResolver[ID, T <: Entity[ID]] {
  this: EntityResolver[ID, T] =>

  /**
   * ページを表すクラス。
   *
   * @author j5ik2o
   */
  case class Page(size: Int, entities: Seq[T])

  /**
   * エンティティをページ単位で検索する。
   *
   * @param pageSize 1ページの件数
   * @param index 検索するページのインデックス
   * @return ページ
   */
  def resolvePage(pageSize: Int, index: Int): Try[Page]
}