package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import CommitInfoAssembler._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {
  var commitInfoDAO: MongoCommitInfoDAO = _

  override def beforeEach() {
    commitInfoDAO = new MongoCommitInfoDAO
  }

  it should "find a stored commit" taggedAs(RequiresDb) in {
    // given
    val commit = randomCommit.get
    commitInfoDAO.storeCommit(commit)

    // when
    val foundCommit = commitInfoDAO.findBySha(commit.sha)

    // then
    foundCommit should be(Some(commit.copy()))
  }

  it should "find stored commit by its id" taggedAs(RequiresDb) in {
    // given
    val commit = randomCommit.get
    commitInfoDAO.storeCommit(commit)

    // when
    val foundCommit = commitInfoDAO.findByCommitId(commit.id)

    // then
    foundCommit should be(Some(commit.copy()))
  }

  it should "store a single commit" taggedAs(RequiresDb) in {
    // given
    val commit = randomCommit.get

    // when
    commitInfoDAO.storeCommit(commit)

    // then
    commitInfoDAO.findBySha(commit.sha) should be('defined)
  }

  it should "retrieve commit sha with last commit date" taggedAs(RequiresDb) in {
    // given
    val date = new DateTime()
    val expectedLastCommit = randomCommit.withAuthorDate(date.minusDays(2)).withCommitDate(date).get
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(2)).withCommitDate(date.minusHours(3)).get)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(12)).withCommitDate(date.minusHours(13)).get)
    commitInfoDAO.storeCommit(expectedLastCommit)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(6)).withCommitDate(date.minusHours(8)).get)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(10)).withCommitDate(date.minusHours(11)).get)

    // when
    val lastSha = commitInfoDAO.findLastSha()

    // then
    lastSha should not be (null)
    lastSha should equal (Some(expectedLastCommit.sha))
  }

  it should "find all commits SHA" taggedAs(RequiresDb) in {
    // given
    val commits = List(CommitInfoAssembler.randomCommit.withSha("111").get, CommitInfoAssembler.randomCommit.withSha("222").get)
    commits.foreach {
      commitInfoDAO.storeCommit(_)
    }

    // when
    val commitsSha = commitInfoDAO.findAllSha()

    // then
    commitsSha should equal(commits.map(_.sha).toSet)
  }

}
