import {
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  onSnapshot,
  Unsubscribe
} from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from './firebase';
import { Pothole, PotholeStatus, UserProfile } from '../types/models';

const POTHOLES_COLLECTION = 'potholes';
const USERS_COLLECTION = 'users';

export const PotholeDbService = {
  /**
   * Listen to real-time changes across the community pothole registry
   */
  subscribeToPotholes(
    onData: (potholes: Pothole[]) => void,
    onError?: (err: Error) => void
  ): Unsubscribe {
    const colRef = collection(db, POTHOLES_COLLECTION);
    return onSnapshot(
      colRef,
      snapshot => {
        const list: Pothole[] = [];
        snapshot.forEach(docSnap => {
          const data = docSnap.data() as Pothole;
          list.push({
            ...data,
            id: docSnap.id
          });
        });
        // Sort descending by reported date
        list.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''));
        onData(list);
      },
      error => {
        handleFirestoreError(error, OperationType.LIST, POTHOLES_COLLECTION);
        if (onError) onError(error instanceof Error ? error : new Error(String(error)));
      }
    );
  },

  /**
   * Create a new pothole report in Firestore
   */
  async createPothole(pothole: Pothole): Promise<void> {
    const docPath = `${POTHOLES_COLLECTION}/${pothole.id}`;
    try {
      const docRef = doc(db, POTHOLES_COLLECTION, pothole.id);
      // Clean undefined values
      const payload: Record<string, any> = { ...pothole };
      Object.keys(payload).forEach(key => {
        if (payload[key] === undefined) delete payload[key];
      });
      await setDoc(docRef, payload);
    } catch (error) {
      handleFirestoreError(error, OperationType.CREATE, docPath);
    }
  },

  /**
   * Increment confirmation upvote (+1 Still There)
   */
  async upvoteHazard(potholeId: string, newCount: number): Promise<void> {
    const docPath = `${POTHOLES_COLLECTION}/${potholeId}`;
    try {
      const docRef = doc(db, POTHOLES_COLLECTION, potholeId);
      await updateDoc(docRef, {
        confirmationCount: newCount,
        updatedAtDisplay: 'Just now'
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, docPath);
    }
  },

  /**
   * Confirm repair vote (transitions to VERIFIED_FIXED after threshold)
   */
  async confirmFixed(
    potholeId: string,
    newFixedCount: number,
    newStatus: PotholeStatus,
    statusNote: string
  ): Promise<void> {
    const docPath = `${POTHOLES_COLLECTION}/${potholeId}`;
    try {
      const docRef = doc(db, POTHOLES_COLLECTION, potholeId);
      await updateDoc(docRef, {
        fixedConfirmationCount: newFixedCount,
        status: newStatus,
        statusNote,
        updatedAtDisplay: 'Just now'
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, docPath);
    }
  },

  /**
   * Delete report (owner only)
   */
  async deletePothole(potholeId: string): Promise<void> {
    const docPath = `${POTHOLES_COLLECTION}/${potholeId}`;
    try {
      const docRef = doc(db, POTHOLES_COLLECTION, potholeId);
      await deleteDoc(docRef);
    } catch (error) {
      handleFirestoreError(error, OperationType.DELETE, docPath);
    }
  },

  /**
   * Fetch user profile from Firestore
   */
  async getUserProfile(userId: string): Promise<UserProfile | null> {
    const docPath = `${USERS_COLLECTION}/${userId}`;
    try {
      const docRef = doc(db, USERS_COLLECTION, userId);
      const snap = await getDoc(docRef);
      if (snap.exists()) {
        return snap.data() as UserProfile;
      }
      return null;
    } catch (error) {
      handleFirestoreError(error, OperationType.GET, docPath);
      return null;
    }
  },

  /**
   * Save or update user profile
   */
  async saveUserProfile(profile: UserProfile): Promise<void> {
    const docPath = `${USERS_COLLECTION}/${profile.userId}`;
    try {
      const docRef = doc(db, USERS_COLLECTION, profile.userId);
      const payload: Record<string, any> = { ...profile };
      Object.keys(payload).forEach(key => {
        if (payload[key] === undefined) delete payload[key];
      });
      await setDoc(docRef, payload, { merge: true });
    } catch (error) {
      handleFirestoreError(error, OperationType.WRITE, docPath);
    }
  }
};
