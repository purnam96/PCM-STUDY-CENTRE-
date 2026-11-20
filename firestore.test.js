const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds
} = require("/usr/local/lib/node_modules/@firebase/rules-unit-testing");
const {
  doc,
  getDoc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  collection,
  query,
  where,
  writeBatch,
  increment,
  deleteField
} = require("/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules/firebase/firestore");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-pcmstudy-70ec7f9f-2177-417f-8273-ddcab4c76f98";

let testEnv;

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

function validThreadPayload(uid, overrides = {}) {
  const ts = pastTimestamp();
  return {
    title: "Why does concave mirror form a virtual image inside focal length?",
    content: "Using mirror formula 1/f = 1/v + 1/u when u < f, v turns positive behind mirror.",
    subject: "PHYSICS",
    academicClass: "CLASS_10",
    chapterName: "Light - Reflection and Refraction",
    tags: ["optics", "mirrors"],
    authorId: uid,
    authorName: "Alice Student",
    authorSchool: "Imphal Valley Academy",
    visibility: "public",
    solved: false,
    upvotesCount: 0,
    repliesCount: 0,
    createdAt: ts,
    updatedAt: ts,
    ...overrides
  };
}

function validReplyPayload(uid, threadId, overrides = {}) {
  const ts = pastTimestamp();
  return {
    threadId,
    authorId: uid,
    authorName: "Bob Peer",
    authorSchool: "Manipur Public School",
    authorBadge: "Student Peer",
    content: "Because 1/v = 1/f - 1/u and with negative u smaller than |f|, 1/v becomes positive!",
    visibility: "public",
    acceptedSolution: false,
    upvotesCount: 0,
    createdAt: ts,
    updatedAt: ts,
    ...overrides
  };
}

test.before(async () => {
  const rules = fs.readFileSync(path.join(__dirname, "firestore.rules"), "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: "127.0.0.1",
      port: 8085,
      rules
    }
  });
});

test.beforeEach(async () => {
  await testEnv.clearFirestore();
});

test.after(async () => {
  await testEnv.cleanup();
});

function getDb(uid) {
  if (!uid) {
    return testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  }
  return testEnv.authenticatedContext(uid, { name: uid }).firestore(DATABASE_ID);
}

test("unauthenticated users cannot read or write threads, replies, or study_materials", async () => {
  const unauthDb = getDb(null);
  await assertFails(getDoc(doc(unauthDb, "threads", "t1")));
  await assertFails(getDocs(query(collection(unauthDb, "threads"), where("visibility", "==", "public"))));
  await assertFails(setDoc(doc(unauthDb, "threads", "t1"), validThreadPayload("anon")));
  await assertFails(getDoc(doc(unauthDb, "study_materials", "m1")));
});

test("authenticated user can create a valid Physics/Chemistry thread and query public threads", async () => {
  const aliceDb = getDb("alice");
  const bobDb = getDb("bob");

  await assertSucceeds(setDoc(doc(aliceDb, "threads", "t-phy-1"), validThreadPayload("alice")));

  // Bob queries matching client query: whereEqualTo("visibility", "public")
  const snap = await assertSucceeds(
    getDocs(query(collection(bobDb, "threads"), where("visibility", "==", "public")))
  );
  assert.equal(snap.size, 1);
  assert.equal(snap.docs[0].data().subject, "PHYSICS");

  // Unfiltered collection read fails zero-trust list rule
  await assertFails(getDocs(collection(bobDb, "threads")));
});

test("rejects spoofed authorId, extra id field, oversized title, or pre-solved create", async () => {
  const aliceDb = getDb("alice");

  await assertFails(
    setDoc(doc(aliceDb, "threads", "bad-author"), validThreadPayload("bob"))
  );
  await assertFails(
    setDoc(doc(aliceDb, "threads", "bad-id-field"), validThreadPayload("alice", { id: "bad-id-field" }))
  );
  await assertFails(
    setDoc(doc(aliceDb, "threads", "bad-title"), validThreadPayload("alice", { title: "x".repeat(161) }))
  );
  await assertFails(
    setDoc(doc(aliceDb, "threads", "bad-solved"), validThreadPayload("alice", { solved: true }))
  );
  await assertFails(
    setDoc(doc(aliceDb, "threads", "bad-upvotes"), validThreadPayload("alice", { upvotesCount: 1 }))
  );
  await assertFails(
    setDoc(doc(aliceDb, "threads", "bad-subject"), validThreadPayload("alice", { subject: "HISTORY" }))
  );
});

test("peer can post reply via atomic WriteBatch and query public replies", async () => {
  const aliceDb = getDb("alice");
  const bobDb = getDb("bob");

  await assertSucceeds(setDoc(doc(aliceDb, "threads", "t1"), validThreadPayload("alice")));

  const batch = writeBatch(bobDb);
  batch.set(doc(bobDb, "threads", "t1", "replies", "r1"), validReplyPayload("bob", "t1"));
  batch.update(doc(bobDb, "threads", "t1"), {
    repliesCount: increment(1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(batch.commit());

  const repliesSnap = await assertSucceeds(
    getDocs(query(collection(aliceDb, "threads", "t1", "replies"), where("visibility", "==", "public")))
  );
  assert.equal(repliesSnap.size, 1);
});

test("only thread author can accept/unaccept a reply as solution; non-author is denied", async () => {
  const aliceDb = getDb("alice");
  const bobDb = getDb("bob");

  await assertSucceeds(setDoc(doc(aliceDb, "threads", "t1"), validThreadPayload("alice")));
  const replyBatch = writeBatch(bobDb);
  replyBatch.set(doc(bobDb, "threads", "t1", "replies", "r1"), validReplyPayload("bob", "t1"));
  replyBatch.update(doc(bobDb, "threads", "t1"), {
    repliesCount: increment(1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(replyBatch.commit());

  // Bob tries to accept his own reply -> must fail
  await assertFails(
    updateDoc(doc(bobDb, "threads", "t1", "replies", "r1"), {
      acceptedSolution: true,
      updatedAt: pastTimestamp()
    })
  );

  // Alice (thread author) accepts Bob's reply cleanly
  const acceptBatch = writeBatch(aliceDb);
  acceptBatch.update(doc(aliceDb, "threads", "t1", "replies", "r1"), {
    acceptedSolution: true,
    updatedAt: pastTimestamp()
  });
  acceptBatch.update(doc(aliceDb, "threads", "t1"), {
    solved: true,
    acceptedReplyId: "r1",
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(acceptBatch.commit());

  // Alice unmarks accepted solution
  const unacceptBatch = writeBatch(aliceDb);
  unacceptBatch.update(doc(aliceDb, "threads", "t1", "replies", "r1"), {
    acceptedSolution: false,
    updatedAt: pastTimestamp()
  });
  unacceptBatch.update(doc(aliceDb, "threads", "t1"), {
    solved: false,
    acceptedReplyId: deleteField(),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(unacceptBatch.commit());
});

test("atomic upvote markers prevent duplicate upvotes and enforce +1/-1 integrity", async () => {
  const aliceDb = getDb("alice");
  const bobDb = getDb("bob");

  await assertSucceeds(setDoc(doc(aliceDb, "threads", "t1"), validThreadPayload("alice")));

  // Direct update without creating upvote marker doc must fail
  await assertFails(
    updateDoc(doc(bobDb, "threads", "t1"), {
      upvotesCount: increment(1),
      updatedAt: pastTimestamp()
    })
  );

  // Valid atomic upvote batch
  const upBatch = writeBatch(bobDb);
  upBatch.set(doc(bobDb, "threads", "t1", "upvotes", "bob"), {
    userId: "bob",
    createdAt: pastTimestamp()
  });
  upBatch.update(doc(bobDb, "threads", "t1"), {
    upvotesCount: increment(1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(upBatch.commit());

  // Second upvote attempt by Bob must fail
  const dupBatch = writeBatch(bobDb);
  dupBatch.set(doc(bobDb, "threads", "t1", "upvotes", "bob"), {
    userId: "bob",
    createdAt: pastTimestamp()
  });
  dupBatch.update(doc(bobDb, "threads", "t1"), {
    upvotesCount: increment(1),
    updatedAt: pastTimestamp()
  });
  await assertFails(dupBatch.commit());

  // Valid atomic un-upvote batch
  const downBatch = writeBatch(bobDb);
  downBatch.delete(doc(bobDb, "threads", "t1", "upvotes", "bob"));
  downBatch.update(doc(bobDb, "threads", "t1"), {
    upvotesCount: increment(-1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(downBatch.commit());
});

test("reply upvote, reply deletion, and cross-user thread delete enforcement", async () => {
  const aliceDb = getDb("alice");
  const bobDb = getDb("bob");

  await assertSucceeds(setDoc(doc(aliceDb, "threads", "t1"), validThreadPayload("alice")));
  const postBatch = writeBatch(bobDb);
  postBatch.set(doc(bobDb, "threads", "t1", "replies", "r1"), validReplyPayload("bob", "t1"));
  postBatch.update(doc(bobDb, "threads", "t1"), {
    repliesCount: increment(1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(postBatch.commit());

  // Alice upvotes Bob's reply
  const replyUpBatch = writeBatch(aliceDb);
  replyUpBatch.set(doc(aliceDb, "threads", "t1", "replies", "r1", "upvotes", "alice"), {
    userId: "alice",
    createdAt: pastTimestamp()
  });
  replyUpBatch.update(doc(aliceDb, "threads", "t1", "replies", "r1"), {
    upvotesCount: increment(1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(replyUpBatch.commit());

  // Bob cannot delete Alice's thread
  await assertFails(deleteDoc(doc(bobDb, "threads", "t1")));

  // Bob can delete his own reply cleanly
  const delReplyBatch = writeBatch(bobDb);
  delReplyBatch.delete(doc(bobDb, "threads", "t1", "replies", "r1"));
  delReplyBatch.update(doc(bobDb, "threads", "t1"), {
    repliesCount: increment(-1),
    updatedAt: pastTimestamp()
  });
  await assertSucceeds(delReplyBatch.commit());

  // Alice can delete her own thread
  await assertSucceeds(deleteDoc(doc(aliceDb, "threads", "t1")));
});

test("study_materials enforces uploaderId ownership and public visibility queries", async () => {
  const aliceDb = getDb("alice");
  const bobDb = getDb("bob");

  await assertSucceeds(
    setDoc(doc(aliceDb, "study_materials", "mat1"), {
      title: "Ohm's Law Notes",
      academicClass: "CLASS_10",
      subject: "PHYSICS",
      chapterName: "Electricity",
      uploaderId: "alice",
      visibility: "public"
    })
  );

  // Bob can list public study_materials
  const snap = await assertSucceeds(
    getDocs(query(collection(bobDb, "study_materials"), where("visibility", "==", "public")))
  );
  assert.equal(snap.size, 1);

  // Bob cannot overwrite or delete Alice's study material
  await assertFails(
    updateDoc(doc(bobDb, "study_materials", "mat1"), { title: "Tampered" })
  );
  await assertFails(deleteDoc(doc(bobDb, "study_materials", "mat1")));
});
