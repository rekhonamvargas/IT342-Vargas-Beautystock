import React from 'react';

interface LogoutConfirmationProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function LogoutConfirmation({ isOpen, onConfirm, onCancel }: LogoutConfirmationProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-8 border border-gray-200">
        {/* Cute Icon */}
        <div className="text-center mb-6">
          <div className="inline-block text-5xl mb-4">👋</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">See you soon!</h2>
          <p className="text-gray-600 text-sm">Are you sure you want to logout?</p>
        </div>

        {/* Decorative Line */}
        <div className="w-12 h-1 bg-gradient-to-r from-pink-500 to-orange-400 rounded-full mx-auto mb-6"></div>

        {/* Buttons */}
        <div className="flex gap-3 mt-8">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-3 border-2 border-gray-300 text-gray-700 font-semibold rounded-lg hover:border-gray-400 hover:bg-gray-50 transition duration-200"
          >
            No, stay
          </button>
          <button
            onClick={onConfirm}
            className="flex-1 px-4 py-3 bg-pink-600 hover:bg-pink-700 text-white font-semibold rounded-lg transition duration-200 shadow-md"
          >
            Yes, logout
          </button>
        </div>

        {/* Cute Footer Message */}
        <p className="text-center text-xs text-gray-500 mt-4">See you later, beauty! 💕</p>
      </div>
    </div>
  );
}
