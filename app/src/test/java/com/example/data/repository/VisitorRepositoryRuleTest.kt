package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Visitor
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import kotlin.test.assertFailsWith

class VisitorRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addVisitor_authenticatedUser_successAndEmittedInFlow() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repo = AppRepository(firestore, auth)

    val visitor = Visitor(
      name = "Alice Test Visitor",
      company = "Alice Corp",
      phone = "555-0101"
    )

    val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addVisitor(visitor)
    }
    assertThat(docId).isNotEmpty()

    val list = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeVisitors(aliceUid).first { it.any { v -> v.id == docId } }
    }
    assertThat(list.map { it.id }).contains(docId)
  }

  @Test
  fun observeVisitors_crossUserAccess_isolated() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = AppRepository(firestore, auth)

    val visitor = Visitor(
      name = "Alice Private Lead",
      company = "Confidential Corp"
    )
    val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.addVisitor(visitor)
    }

    val bobUid = signInTestUser(BOB_EMAIL)
    val bobRepo = AppRepository(firestore, auth)

    val exception = assertFailsWith<FirebaseFirestoreException> {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeVisitors(aliceUid).first()
      }
    }
    assertThat(exception.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
  }

  @Test
  fun addVisitor_unauthenticatedUser_failsFast() = runBlocking {
    auth.signOut()
    val repo = AppRepository(firestore, auth)

    val visitor = Visitor(name = "Unauth Visitor")
    val exception = assertFailsWith<IllegalStateException> {
      repo.addVisitor(visitor)
    }
    assertThat(exception.message).contains("User must be signed in with Google")
  }

  private companion object {
    const val ALICE_EMAIL = "alice@example.com"
    const val BOB_EMAIL = "bob@example.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
