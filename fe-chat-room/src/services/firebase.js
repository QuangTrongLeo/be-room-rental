// // =============================================
// // Firebase Configuration
// // =============================================
// // Điền thông tin Firebase project của bạn ở đây
// // Lấy từ: Firebase Console > Project Settings > Your apps

// import { initializeApp } from 'firebase/app'
// import { getDatabase } from 'firebase/database'
// import { getFirestore } from 'firebase/firestore'

// const firebaseConfig = {
//   apiKey: import.meta.env.VITE_FIREBASE_API_KEY || 'YOUR_API_KEY',
//   authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || 'YOUR_AUTH_DOMAIN',
//   // databaseURL: import.meta.env.VITE_FIREBASE_DATABASE_URL || 'YOUR_DATABASE_URL',
//   projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || 'YOUR_PROJECT_ID',
//   storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || 'YOUR_STORAGE_BUCKET',
//   messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || 'YOUR_SENDER_ID',
//   appId: import.meta.env.VITE_FIREBASE_APP_ID || 'YOUR_APP_ID',
// }

// const app = initializeApp(firebaseConfig)

// // Sử dụng Realtime Database để chat realtime
// export const db = getDatabase(app)

// // Hoặc Firestore (tùy BE config)
// export const firestore = getFirestore(app)

// export default app


// =============================================
// Firebase Configuration (CHỈ DÙNG FIRESTORE)
// =============================================

import { initializeApp } from 'firebase/app'
import { getFirestore } from 'firebase/firestore' // Chỉ import Firestore

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  // Xóa bỏ dòng databaseURL vì Firestore tự động nhận diện qua projectId
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

const app = initializeApp(firebaseConfig)

// Gán biến db trỏ thẳng vào Firestore. 
// Từ nay các file khác chỉ cần import { db } là gọi được Firestore.
export const db = getFirestore(app)

export default app