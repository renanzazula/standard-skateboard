# Settings Functionality Documentation

## Table of Contents
1. [Overview](#overview)
2. [Role-Based Access Control](#role-based-access-control)
3. [Guest User Settings](#guest-user-settings)
4. [Standard User Settings](#standard-user-settings)
5. [Admin Settings](#admin-settings)
6. [API Reference](#api-reference)
7. [Navigation Flow](#navigation-flow)
8. [Use Cases by Role](#use-cases-by-role)

---

## Overview

The settings system provides role-based configuration management with three distinct user roles:
- **Guest**: Unauthenticated users (no settings access)
- **Standard User**: Authenticated users with basic settings
- **Admin**: Full access to system configuration and user management

**Main Entry Point**: `/settings` (Tab Navigation)

---

## Role-Based Access Control

### Access Matrix

| Feature | Guest | Standard User | Admin |
|---------|-------|---------------|-------|
| Profile Settings | ❌ | ✅ | ✅ |
| Appearance Settings | ❌ | ✅ | ✅ |
| Language Selection | ❌ | ✅ | ✅ |
| User Management | ❌ | ❌ | ✅ |
| Authentication Config | ❌ | ❌ | ✅ |
| Session Configuration | ❌ | ❌ | ✅ |
| Language Management | ❌ | ❌ | ✅ |
| Profile Restrictions | ❌ | ❌ | ✅ |
| Navigation Management | ❌ | ❌ | ✅ |

---

## Guest User Settings

### Available Features
**None** - Guests must authenticate to access settings.

### Navigation Flow
```
Guest → Login Required → /login
```

### Use Cases
1. **UC-G1**: Guest attempts to access settings
    - **Trigger**: Navigate to settings tab
    - **Action**: Redirect to login screen
    - **Result**: User must authenticate

---

## Standard User Settings

### 1. Profile Settings

#### 1.1 Profile Information Card
**Location**: Settings Screen (Top Section)

**Available Actions**:
- **View Profile Data**
    - Avatar (image or default icon)
    - Username/Name
    - Email (read-only)
    - Role badge (read-only)
    - Provider badge (read-only)

- **Change Avatar**
    - Tap on avatar/camera icon
    - Choose from:
        - Take Photo (Camera)
        - Choose from Gallery

**API Functions**:
```typescript
// Update profile avatar
updateProfile({ avatar: string })

// Context: AuthContext
const { user, updateProfile } = useAuth();
```

**Validation**:
- Avatar file size: Up to `config.profileConfig.avatarMaxSizeMB` (default: 5MB)
- Allowed formats: PNG, JPEG, JPG, SVG
- File must have `file://` URI prefix

**Storage**: AsyncStorage key `@user_data`

---

#### 1.2 Edit Username
**Location**: Profile Settings Card → Edit Button

**Flow**:
```
Settings → Edit Username Button → Modal → Input Username → Save
```

**API Functions**:
```typescript
// Update username
updateProfile({ username: string })
```

**Validation**:
- Min length: `config.profileConfig.usernameMinLength` (default: 3)
- Max length: `config.profileConfig.usernameMaxLength` (default: 30)
- Trimmed whitespace

**Storage**: AsyncStorage key `@user_data`

---

#### 1.3 Language Selection
**Location**: Profile Settings Card → Language Selector

**Flow**:
```
Settings → Language Selector → Language Modal → Select Language → Apply
```

**API Functions**:
```typescript
// Change language
setLanguage(language: Language)

// Context: PreferencesContext
const { language, setLanguage } = usePreferences();
```

**Available Languages**:
Only languages enabled by admin in `config.languageConfig.availableLanguages`

**Storage**: AsyncStorage key `@user_language_{userId}`

**Behavior**:
- Language selection is user-specific
- Persists across sessions
- Falls back to default language if not set

---

### 2. Appearance Settings

#### 2.1 Dark Mode Toggle
**Location**: Appearance Section

**Flow**:
```
Settings → Dark Mode Toggle → Apply Instantly
```

**API Functions**:
```typescript
// Toggle theme
toggleTheme()

// Or set specific theme
setTheme(theme: 'light' | 'dark')

// Context: PreferencesContext
const { theme, toggleTheme } = usePreferences();
```

**Storage**: AsyncStorage key `@user_theme`

**Behavior**:
- Changes apply immediately
- Global setting (not user-specific)
- Persists across app restarts

---

### 3. Account Settings

#### 3.1 Logout
**Location**: Account Section

**Flow**:
```
Settings → Logout Button → Confirmation Dialog → Logout → Redirect to Login
```

**API Functions**:
```typescript
// Logout user
await logout()

// Context: AuthContext
const { logout } = useAuth();
```

**Actions**:
1. Show confirmation dialog
2. Clear session from AsyncStorage (`@user_data`, `@session_data`)
3. Clear session timeout
4. Update auth state
5. Navigate to `/login`

---

## Admin Settings

All Standard User features **PLUS** the following administrative features:

### 4. User Management

#### 4.1 Overview
**Route**: `/user-management`  
**Access**: Admin only  
**Entry Point**: Settings → Admin Configuration → User Management

**Flow**:
```
Settings → User Management → View All Users → [Filter/Search/Edit/Disable/Delete]
```

---

#### 4.2 View All Users

**Features**:
- **Statistics Dashboard**
    - Total Users
    - Active Users
    - Disabled Users

- **User List Display**
    - Avatar/Icon
    - Name/Username
    - Email
    - Role (Admin/Standard)
    - Status (Active/Disabled)
    - Provider (Google/Apple/Manual)
    - Created Date
    - Last Login Date

**API Functions**:
```typescript
// Context: UserManagementContext
const { users } = useUserManagement();
```

**Storage**: AsyncStorage key `@managed_users`

---

#### 4.3 Search & Filter Users

**Search Fields**:
- Name
- Email
- Username

**Filters**:
- **Role Filter**: All, Admin, Standard
- **Status Filter**: All, Active, Disabled

**Flow**:
```
User Management → Search/Filter Button → Select Filters → Apply
```

**Implementation**:
```typescript
const filteredUsers = useMemo(() => {
  let filtered = users;
  
  // Search
  if (searchQuery) {
    filtered = filtered.filter(user =>
      user.name.includes(searchQuery) ||
      user.email.includes(searchQuery) ||
      user.username?.includes(searchQuery)
    );
  }
  
  // Filter by role
  if (roleFilter !== 'all') {
    filtered = filtered.filter(user => user.role === roleFilter);
  }
  
  // Filter by status
  if (statusFilter !== 'all') {
    filtered = filtered.filter(user => user.status === statusFilter);
  }
  
  return filtered;
}, [users, searchQuery, roleFilter, statusFilter]);
```

---

#### 4.4 Edit User

**Route**: User Management → Edit Button → Edit Modal  
**Editable Fields**:
- Name
- Username
- Role (Admin/Standard)

**API Functions**:
```typescript
// Update user
await updateUser(userId: string, updates: {
  name?: string;
  username?: string;
  role?: 'admin' | 'standard';
})

// Context: UserManagementContext
const { updateUser } = useUserManagement();
```

**Flow**:
```
User Card → Edit Button → Edit Modal → Change Fields → Save → Success
```

**Validation**:
- Name is required (cannot be empty)
- Username is optional
- Role change is immediate

---

#### 4.5 Disable/Enable User

**API Functions**:
```typescript
// Toggle user status
await toggleUserStatus(userId: string)

// Context: UserManagementContext
const { toggleUserStatus } = useUserManagement();
```

**Flow**:
```
User Card → Disable/Enable Button → Confirmation → Apply → Success
```

**Behavior**:
- Disabled users cannot log in
- Status check happens during authentication
- Admin cannot disable their own account

**Authentication Check**:
```typescript
// In AuthContext during login
const isDisabled = await checkUserStatus(userId);
if (isDisabled) {
  throw new Error('User account is disabled');
}
```

---

#### 4.6 Delete User

**API Functions**:
```typescript
// Delete user
await deleteUser(userId: string)

// Context: UserManagementContext
const { deleteUser } = useUserManagement();
```

**Flow**:
```
User Card → Delete Button → Confirmation → Delete → Success
```

**Restrictions**:
- Admin cannot delete their own account
- Deletion is permanent
- Requires confirmation dialog

---

### 5. Configure Authentication

#### 5.1 Overview
**Route**: `/configure-authentication`  
**Access**: Admin only  
**Entry Point**: Settings → Admin Configuration → Configure Authentication

**Flow**:
```
Settings → Configure Authentication → [Enable/Disable Methods] → [Set Service Mode]
```

---

#### 5.2 Authentication Methods

**Available Methods**:
1. **Google Authentication**
2. **Apple Authentication**
3. **Manual Registration** (Email/Password)

**For Each Method**:
- Enable/Disable toggle
- Service Mode selection (Mock/Real)

---

#### 5.3 Enable/Disable Authentication Method

**API Functions**:
```typescript
// Toggle authentication method
toggleAuthMethod(method: 'google' | 'apple' | 'manual')

// Context: AdminConfigContext
const { config, toggleAuthMethod } = useAdminConfig();
```

**Configuration Storage**:
```typescript
config.enabledAuthMethods = {
  google: boolean,
  apple: boolean,
  manual: boolean
}
```

**Behavior**:
- Changes apply immediately
- Affects login/signup screens
- At least one method must remain enabled (system prevents disabling all)

---

#### 5.4 Set Service Mode

**API Functions**:
```typescript
// Set service mode
setServiceMode(method: 'google' | 'apple' | 'manual', mode: 'mock' | 'real')

// Context: AdminConfigContext
const { setServiceMode } = useAdminConfig();
```

**Modes**:
- **Mock**: Uses simulated authentication (for testing)
- **Real**: Uses actual OAuth/authentication services (for production)

**Configuration Storage**:
```typescript
config.serviceModes = {
  google: 'mock' | 'real',
  apple: 'mock' | 'real',
  manual: 'mock' | 'real'
}
```

---

### 6. Session Configuration

#### 6.1 Overview
**Route**: `/session-configuration`  
**Access**: Admin only  
**Entry Point**: Settings → Admin Configuration → Session Configuration

**Flow**:
```
Settings → Session Configuration → Adjust Timers → Toggle Auto-Refresh → Save
```

---

#### 6.2 Maximum Session Time

**Description**: Total active session lifetime before forced logout

**API Functions**:
```typescript
// Update session config
updateSessionConfig({ maxTime: number })

// Context: AdminConfigContext
const { config, updateSessionConfig } = useAdminConfig();
```

**Configuration**:
- **Range**: 5 minutes - 24 hours
- **Step**: 5 minutes
- **Default**: 30 minutes (1800000ms)
- **Format**: Milliseconds

**Controls**:
- Decrease button (-)
- Current value display
- Increase button (+)

**Time Display Format**:
```typescript
// < 60 min: "X min"
// >= 60 min: "Xh Ym" or "X hr"
```

---

#### 6.3 Idle Timeout

**Description**: Time of inactivity before automatic logout

**API Functions**:
```typescript
// Update idle timeout
updateSessionConfig({ idleTime: number })
```

**Configuration**:
- **Range**: 5 minutes - 24 hours
- **Step**: 5 minutes
- **Default**: 15 minutes (900000ms)
- **Format**: Milliseconds

**Behavior**:
- Timer resets on user activity (if auto-refresh enabled)
- Triggers logout when expired
- Implemented using setTimeout

**Implementation**:
```typescript
// In AuthContext
const startSessionTimeout = () => {
  clearSessionTimeout();
  sessionTimeoutRef.current = setTimeout(() => {
    console.log('Session timeout - logging out');
    logout();
  }, config.sessionConfig.idleTime);
};
```

---

#### 6.4 Auto Refresh Session

**Description**: Extend session automatically while user is active

**API Functions**:
```typescript
// Toggle auto-refresh
updateSessionConfig({ autoRefresh: boolean })
```

**Configuration**:
- **Type**: Boolean toggle
- **Default**: true

**Behavior**:
- When enabled: Session extends on user activity
- When disabled: Session expires after idle timeout regardless of activity
- Activity tracking via `updateActivity()` function

**Implementation**:
```typescript
// In AuthContext
const updateActivity = () => {
  if (authState.isAuthenticated && config.sessionConfig.autoRefresh) {
    const now = Date.now();
    setAuthState(prev => ({ ...prev, lastActivity: now }));
    AsyncStorage.setItem('@session_data', JSON.stringify({ lastActivity: now }));
  }
};
```

---

### 7. Language Settings

#### 7.1 Overview
**Route**: `/language-settings`  
**Access**: Admin only  
**Entry Point**: Settings → Admin Configuration → Language Settings

**Flow**:
```
Settings → Language Settings → Enable/Disable Languages → Set Default Language
```

---

#### 7.2 Manage Available Languages

**API Functions**:
```typescript
// Toggle language availability
toggleLanguageAvailability(language: Language)

// Context: AdminConfigContext
const { toggleLanguageAvailability } = useAdminConfig();
```

**Available Languages**:
- English (en) 🇬🇧
- Spanish (es) 🇪🇸

**Language Object Structure**:
```typescript
{
  code: string,
  name: string,
  nativeName: string,
  flag: string
}
```

---

#### 7.3 Enable/Disable Language

**Flow**:
```
Language Settings → Toggle Language → [Confirmation if disabling] → Apply
```

**Rules**:
- At least one language must remain enabled
- Cannot disable the default language
- Must set a different default before disabling current default

**Confirmation Dialogs**:
- **Disable**: "Are you sure you want to disable [Language]? Users will no longer be able to select this language."
- **Cannot Disable Default**: "Cannot disable the default language. Please set a different language as default first."
- **Cannot Disable Last**: "At least one language must remain enabled."

---

#### 7.4 Set Default Language

**API Functions**:
```typescript
// Set default language
setDefaultLanguage(language: Language)

// Context: AdminConfigContext
const { setDefaultLanguage } = useAdminConfig();
```

**Flow**:
```
Language Settings → Language Row → "Set Default" Button → Confirmation → Apply
```

**Behavior**:
- Only enabled languages can be set as default
- Default language is used for:
    - New users (first time)
    - Guest users
    - Fallback if user's language becomes unavailable

**Confirmation**:
"Set [Language] as the default language? This will be used for new users and as a fallback."

---

#### 7.5 Remove Language

**API Functions**:
```typescript
// Same as toggle, but with delete action
toggleLanguageAvailability(language: Language)
```

**Flow**:
```
Language Settings → Delete Button → Confirmation → Remove
```

**Rules**:
- Cannot remove default language
- Cannot remove last remaining language
- Language must be enabled to remove

---

### 8. Profile Restrictions

#### 8.1 Overview
**Route**: `/profile-restrictions`  
**Access**: Admin only  
**Entry Point**: Settings → Admin Configuration → Profile Restrictions

**Flow**:
```
Settings → Profile Restrictions → Adjust Username/Avatar Policies → Save
```

---

#### 8.2 Username Length Restrictions

**API Functions**:
```typescript
// Update profile config
updateProfileConfig({
  usernameMinLength?: number,
  usernameMaxLength?: number
})

// Context: AdminConfigContext
const { config, updateProfileConfig } = useAdminConfig();
```

**Minimum Length**:
- **Range**: 1 - (maxLength - 1)
- **Step**: 1
- **Default**: 3
- **Controls**: +/- buttons

**Maximum Length**:
- **Range**: (minLength + 1) - ∞
- **Step**: 1
- **Default**: 30
- **Controls**: +/- buttons

**Validation Logic**:
```typescript
// Ensure min < max
const newMin = Math.min(currentMax - 1, requestedMin);
const newMax = Math.max(currentMin + 1, requestedMax);
```

---

#### 8.3 Avatar File Size Restrictions

**API Functions**:
```typescript
// Update avatar size limit
updateProfileConfig({ avatarMaxSizeMB: number })
```

**Configuration**:
- **Range**: 1 MB - ∞
- **Step**: 1 MB
- **Default**: 5 MB
- **Controls**: +/- buttons

**Allowed Formats**:
```typescript
config.profileConfig.allowedAvatarFormats = [
  'image/png',
  'image/jpeg',
  'image/jpg',
  'image/svg+xml'
]
```

---

### 9. Navigation Management

#### 9.1 Overview
**Route**: `/navigation-management`  
**Access**: Admin only  
**Entry Point**: Settings → Admin Configuration → Navigation Management

**Flow**:
```
Settings → Navigation Management → [Enable/Disable Tabs] → [Reorder Tabs] → [Edit Names] → [Add/Remove Custom Tabs]
```

---

#### 9.2 Tab Structure

**NavigationTab Interface**:
```typescript
interface NavigationTab {
  id: string;           // Unique identifier
  name: string;         // Display name
  enabled: boolean;     // Visibility toggle
  icon: string;         // Icon name
  order: number;        // Display order
  isSystem: boolean;    // System vs custom tab
}
```

**System Tabs** (cannot be deleted):
- home
- feed
- skate-square
- podcast
- settings (always visible)

---

#### 9.3 Enable/Disable Tabs

**API Functions**:
```typescript
// Toggle tab visibility
toggleTabEnabled(tabId: string)

// Context: AdminConfigContext
const { toggleTabEnabled } = useAdminConfig();
```

**Flow**:
```
Navigation Management → Tab Toggle Switch → Apply Instantly
```

**Special Rule**:
- **Settings Tab**: Cannot be disabled
- Shows alert: "Settings tab must always be visible for system administration"

---

#### 9.4 Reorder Tabs

**API Functions**:
```typescript
// Update tab order
updateTabOrder(tabId: string, newOrder: number)

// Context: AdminConfigContext
const { updateTabOrder } = useAdminConfig();
```

**Controls**:
- **Move Up** (ChevronUp): Decrease order
- **Move Down** (ChevronDown): Increase order

**Flow**:
```
Navigation Management → Move Up/Down Buttons → Reorder → Apply Instantly
```

**Order Logic**:
```typescript
// Moving tab affects other tabs' order
if (oldOrder < newOrder) {
  // Moving down: shift intermediate tabs up
  tabs.filter(t => t.order > oldOrder && t.order <= newOrder)
    .forEach(t => t.order -= 1);
} else {
  // Moving up: shift intermediate tabs down
  tabs.filter(t => t.order >= newOrder && t.order < oldOrder)
    .forEach(t => t.order += 1);
}
```

---

#### 9.5 Edit Tab Name

**API Functions**:
```typescript
// Update tab name
updateTabName(tabId: string, name: string)

// Context: AdminConfigContext
const { updateTabName } = useAdminConfig();
```

**Flow**:
```
Navigation Management → Edit Button → Modal → Input New Name → Save → Success
```

**Validation**:
- Name cannot be empty
- Whitespace is trimmed

---

#### 9.6 Add Custom Tab

**API Functions**:
```typescript
// Add custom tab
addCustomTab(tab: {
  id: string,
  name: string,
  enabled: boolean,
  icon: string
})

// Context: AdminConfigContext
const { addCustomTab } = useAdminConfig();
```

**Flow**:
```
Navigation Management → Add Custom Tab Button → Modal → Fill Details → Add → Success
```

**Required Fields**:
- **Tab ID**: Unique identifier (e.g., "profile", "about")
- **Tab Name**: Display name (e.g., "Profile", "About")
- **Tab Icon**: Icon name (e.g., "user", "info")

**Validation**:
- Tab ID must be unique
- Tab ID and Name are required
- Tab is automatically enabled
- Order is set to max + 1

**Tab Creation**:
```typescript
const newTab: NavigationTab = {
  ...providedFields,
  isSystem: false,
  order: maxOrder + 1
};
```

---

#### 9.7 Remove Custom Tab

**API Functions**:
```typescript
// Remove custom tab
removeCustomTab(tabId: string)

// Context: AdminConfigContext
const { removeCustomTab } = useAdminConfig();
```

**Flow**:
```
Navigation Management → Delete Button → Confirmation → Remove → Success
```

**Rules**:
- Only custom tabs (isSystem: false) can be removed
- System tabs show no delete button
- Requires confirmation dialog

---

## API Reference

### Auth Context

**Import**:
```typescript
import { useAuth } from '@/contexts/AuthContext';
```

**Functions**:
```typescript
// User state
user: User | null
isAuthenticated: boolean
isLoading: boolean

// Authentication
loginWithCredentials(email: string, password: string): Promise<boolean>
loginWithGoogle(): Promise<boolean>
loginWithApple(): Promise<boolean>
signUp(email: string, password: string, name: string, provider: string): Promise<boolean>
logout(): Promise<void>

// Profile management
updateProfile(updates: { username?: string, avatar?: string }): Promise<void>

// Session management
updateActivity(): void
resetPassword(email: string): Promise<boolean>
```

**Storage Keys**:
- `@user_data`: User object
- `@session_data`: Session timestamp
- `@managed_users`: Managed users array

---

### Admin Config Context

**Import**:
```typescript
import { useAdminConfig } from '@/contexts/AdminConfigContext';
```

**Functions**:
```typescript
// Config state
config: AdminConfig
isLoading: boolean

// Authentication methods
toggleAuthMethod(method: AuthMethod): void
setServiceMode(method: AuthMethod, mode: ServiceMode): void

// Session configuration
updateSessionConfig(config: Partial<SessionConfig>): void

// Language management
toggleLanguageAvailability(language: Language): void
setDefaultLanguage(language: Language): void

// Profile restrictions
updateProfileConfig(config: Partial<ProfileConfig>): void

// Navigation management
toggleTabEnabled(tabId: string): void
updateTabName(tabId: string, name: string): void
addCustomTab(tab: Omit<NavigationTab, 'isSystem' | 'order'>): void
removeCustomTab(tabId: string): void
updateTabOrder(tabId: string, newOrder: number): void
```

**Storage Keys**:
- `@admin_config`: Admin configuration object

---

### Preferences Context

**Import**:
```typescript
import { usePreferences } from '@/contexts/PreferencesContext';
```

**Functions**:
```typescript
// Theme
theme: 'light' | 'dark'
colors: ThemeColors
setTheme(theme: Theme): Promise<void>
toggleTheme(): void

// Language
language: Language
setLanguage(language: Language): Promise<void>

// Timezone & Date Format
timezone: string
setTimezone(timezone: string): Promise<void>
dateFormat: DateFormat
setDateFormat(format: DateFormat): Promise<void>

// User preferences
loadUserPreferences(userId: string, defaults?): Promise<void>
clearUserPreferences(): Promise<void>

// State
isLoading: boolean
```

**Storage Keys**:
- `@user_theme`: Global theme
- `@user_language_{userId}`: User-specific language
- `@user_timezone_{userId}`: User-specific timezone
- `@user_dateformat_{userId}`: User-specific date format

---

### User Management Context

**Import**:
```typescript
import { useUserManagement } from '@/contexts/UserManagementContext';
```

**Functions**:
```typescript
// Users state
users: ManagedUser[]
isLoading: boolean

// User management
toggleUserStatus(userId: string): Promise<void>
deleteUser(userId: string): Promise<void>
updateUser(userId: string, updates: Partial<ManagedUser>): Promise<void>
loadUsers(): Promise<void>
```

**Storage Keys**:
- `@managed_users`: Array of managed users

---

## Navigation Flow

### Settings Screen Navigation Map

```
App Root
│
├─ /(tabs)/settings [AUTHENTICATED]
│  │
│  ├─ Profile Settings Section
│  │  ├─ Avatar Tap → [Camera Picker / Gallery Picker]
│  │  ├─ Edit Username → [Username Modal]
│  │  └─ Language Selector → [Language Modal]
│  │
│  ├─ Appearance Section
│  │  └─ Dark Mode Toggle → [Instant Apply]
│  │
│  ├─ Admin Configuration Section [ADMIN ONLY]
│  │  │
│  │  ├─ User Management → /user-management
│  │  │  ├─ Search/Filter → [Filter Modal]
│  │  │  ├─ Edit User → [Edit Modal]
│  │  │  ├─ Toggle Status → [Confirmation]
│  │  │  └─ Delete User → [Confirmation]
│  │  │
│  │  ├─ Configure Authentication → /configure-authentication
│  │  │  ├─ Toggle Methods → [Instant Apply]
│  │  │  └─ Service Mode → [Instant Apply]
│  │  │
│  │  ├─ Session Configuration → /session-configuration
│  │  │  ├─ Max Session Time → [Adjust with +/-]
│  │  │  ├─ Idle Timeout → [Adjust with +/-]
│  │  │  └─ Auto Refresh → [Toggle]
│  │  │
│  │  ├─ Language Settings → /language-settings
│  │  │  ├─ Toggle Languages → [Enable/Disable]
│  │  │  ├─ Set Default → [Confirmation]
│  │  │  └─ Remove Language → [Confirmation]
│  │  │
│  │  ├─ Profile Restrictions → /profile-restrictions
│  │  │  ├─ Username Min/Max → [Adjust with +/-]
│  │  │  └─ Avatar Max Size → [Adjust with +/-]
│  │  │
│  │  └─ Navigation Management → /navigation-management
│  │     ├─ Toggle Tabs → [Enable/Disable]
│  │     ├─ Reorder Tabs → [Move Up/Down]
│  │     ├─ Edit Tab Name → [Edit Modal]
│  │     ├─ Add Custom Tab → [Add Modal]
│  │     └─ Remove Custom Tab → [Confirmation]
│  │
│  └─ Account Section
│     └─ Logout → [Confirmation] → /login
```

---

## Use Cases by Role

### Guest User Use Cases

#### UC-G1: Attempt to Access Settings
- **Actor**: Guest User
- **Precondition**: Not authenticated
- **Flow**:
    1. Guest taps on Settings tab
    2. System checks authentication status
    3. System redirects to `/login`
- **Postcondition**: User is on login screen

---

### Standard User Use Cases

#### UC-U1: Change Avatar
- **Actor**: Standard User
- **Precondition**: Authenticated
- **Flow**:
    1. User navigates to Settings
    2. User taps on avatar or camera icon
    3. System shows action sheet
    4. User selects "Take Photo" or "Choose from Gallery"
    5. System requests camera/gallery permission
    6. User grants permission
    7. User takes/selects photo
    8. System crops image (1:1 aspect ratio)
    9. System validates file size and format
    10. System calls `updateProfile({ avatar: uri })`
    11. System updates UI
    12. System shows success message
- **Postcondition**: User avatar is updated
- **Alternative Flows**:
    - Permission denied → Show error alert
    - File too large → Show error alert
    - Invalid format → Show error alert

#### UC-U2: Edit Username
- **Actor**: Standard User
- **Precondition**: Authenticated
- **Flow**:
    1. User navigates to Settings
    2. User taps "Edit" button next to username
    3. System opens username modal
    4. User enters new username
    5. User taps "Save"
    6. System validates length (min/max)
    7. System calls `updateProfile({ username })`
    8. System closes modal
    9. System shows success message
- **Postcondition**: Username is updated
- **Alternative Flows**:
    - Too short → Show error alert
    - Too long → Show error alert

#### UC-U3: Change Language
- **Actor**: Standard User
- **Precondition**: Authenticated
- **Flow**:
    1. User navigates to Settings
    2. User taps language selector
    3. System opens language modal
    4. System shows only enabled languages
    5. User selects language
    6. System calls `setLanguage(language)`
    7. System closes modal
    8. System updates UI with new language
- **Postcondition**: User interface displays in selected language

#### UC-U4: Toggle Dark Mode
- **Actor**: Standard User
- **Precondition**: Authenticated
- **Flow**:
    1. User navigates to Settings
    2. User toggles Dark Mode switch
    3. System calls `toggleTheme()`
    4. System updates theme instantly
    5. System saves preference
- **Postcondition**: App theme is changed

#### UC-U5: Logout
- **Actor**: Standard User
- **Precondition**: Authenticated
- **Flow**:
    1. User navigates to Settings
    2. User taps "Logout" button
    3. System shows confirmation dialog
    4. User confirms logout
    5. System calls `logout()`
    6. System clears session data
    7. System navigates to `/login`
- **Postcondition**: User is logged out

---

### Admin User Use Cases

#### UC-A1: View All Users
- **Actor**: Admin
- **Precondition**: Authenticated as admin
- **Flow**:
    1. Admin navigates to Settings
    2. Admin taps "User Management"
    3. System loads all users from storage
    4. System displays user list with statistics
    5. System shows Total/Active/Disabled counts
- **Postcondition**: Admin sees all users

#### UC-A2: Search Users
- **Actor**: Admin
- **Precondition**: On User Management screen
- **Flow**:
    1. Admin types in search field
    2. System filters users in real-time
    3. System searches name, email, username
    4. System updates displayed list
- **Postcondition**: Filtered user list is displayed

#### UC-A3: Filter Users by Role/Status
- **Actor**: Admin
- **Precondition**: On User Management screen
- **Flow**:
    1. Admin taps filter button
    2. System opens filter modal
    3. Admin selects role filter (All/Admin/Standard)
    4. Admin selects status filter (All/Active/Disabled)
    5. Admin closes modal
    6. System applies filters
    7. System updates displayed list
- **Postcondition**: Filtered user list is displayed

#### UC-A4: Edit User Information
- **Actor**: Admin
- **Precondition**: On User Management screen
- **Flow**:
    1. Admin taps "Edit" on user card
    2. System opens edit modal
    3. Admin edits name/username/role
    4. Admin taps "Save"
    5. System validates input
    6. System calls `updateUser(userId, updates)`
    7. System closes modal
    8. System refreshes user list
    9. System shows success message
- **Postcondition**: User information is updated

#### UC-A5: Disable User Account
- **Actor**: Admin
- **Precondition**: On User Management screen
- **Flow**:
    1. Admin taps "Disable" on user card
    2. System shows confirmation dialog
    3. Admin confirms action
    4. System calls `toggleUserStatus(userId)`
    5. System updates user status to "disabled"
    6. System refreshes UI
    7. System shows success message
- **Postcondition**: User cannot log in
- **Alternative Flows**:
    - Admin tries to disable self → Show error

#### UC-A6: Enable User Account
- **Actor**: Admin
- **Precondition**: On User Management screen, user is disabled
- **Flow**:
    1. Admin taps "Enable" on user card
    2. System shows confirmation dialog
    3. Admin confirms action
    4. System calls `toggleUserStatus(userId)`
    5. System updates user status to "active"
    6. System refreshes UI
    7. System shows success message
- **Postcondition**: User can log in again

#### UC-A7: Delete User
- **Actor**: Admin
- **Precondition**: On User Management screen
- **Flow**:
    1. Admin taps "Delete" on user card
    2. System shows confirmation dialog
    3. Admin confirms deletion
    4. System calls `deleteUser(userId)`
    5. System removes user from storage
    6. System refreshes user list
    7. System shows success message
- **Postcondition**: User is permanently deleted
- **Alternative Flows**:
    - Admin tries to delete self → Show error

#### UC-A8: Configure Authentication Methods
- **Actor**: Admin
- **Precondition**: On Configure Authentication screen
- **Flow**:
    1. Admin navigates to Configure Authentication
    2. Admin toggles authentication method (Google/Apple/Manual)
    3. System calls `toggleAuthMethod(method)`
    4. System updates configuration
    5. If enabled, admin selects service mode (Mock/Real)
    6. System calls `setServiceMode(method, mode)`
    7. System saves configuration
- **Postcondition**: Authentication configuration is updated
- **Alternative Flows**:
    - Try to disable all methods → System prevents action

#### UC-A9: Configure Session Timeouts
- **Actor**: Admin
- **Precondition**: On Session Configuration screen
- **Flow**:
    1. Admin navigates to Session Configuration
    2. Admin adjusts max session time using +/- buttons
    3. System calls `updateSessionConfig({ maxTime })`
    4. Admin adjusts idle timeout using +/- buttons
    5. System calls `updateSessionConfig({ idleTime })`
    6. Admin toggles auto-refresh
    7. System calls `updateSessionConfig({ autoRefresh })`
    8. System saves configuration
- **Postcondition**: Session behavior is updated for all users

#### UC-A10: Manage Available Languages
- **Actor**: Admin
- **Precondition**: On Language Settings screen
- **Flow**:
    1. Admin navigates to Language Settings
    2. Admin views list of all languages
    3. Admin toggles language availability
    4. System calls `toggleLanguageAvailability(language)`
    5. System validates rules (not default, not last)
    6. System updates configuration
    7. If disabling, system shows confirmation
    8. System saves configuration
- **Postcondition**: Language availability is updated
- **Alternative Flows**:
    - Try to disable default → Show error
    - Try to disable last language → Show error

#### UC-A11: Set Default Language
- **Actor**: Admin
- **Precondition**: On Language Settings screen
- **Flow**:
    1. Admin navigates to Language Settings
    2. Admin taps "Set Default" on enabled language
    3. System shows confirmation dialog
    4. Admin confirms action
    5. System calls `setDefaultLanguage(language)`
    6. System updates configuration
    7. System shows success message
- **Postcondition**: Default language is updated

#### UC-A12: Configure Profile Restrictions
- **Actor**: Admin
- **Precondition**: On Profile Restrictions screen
- **Flow**:
    1. Admin navigates to Profile Restrictions
    2. Admin adjusts username min length using +/- buttons
    3. System calls `updateProfileConfig({ usernameMinLength })`
    4. Admin adjusts username max length using +/- buttons
    5. System calls `updateProfileConfig({ usernameMaxLength })`
    6. Admin adjusts avatar max size using +/- buttons
    7. System calls `updateProfileConfig({ avatarMaxSizeMB })`
    8. System saves configuration
- **Postcondition**: Profile restrictions are updated
- **Validation**: min < max always

#### UC-A13: Enable/Disable Navigation Tabs
- **Actor**: Admin
- **Precondition**: On Navigation Management screen
- **Flow**:
    1. Admin navigates to Navigation Management
    2. Admin views list of tabs (sorted by order)
    3. Admin toggles tab visibility
    4. System calls `toggleTabEnabled(tabId)`
    5. System updates configuration
    6. System shows instant preview (if possible)
- **Postcondition**: Tab visibility is updated
- **Alternative Flows**:
    - Try to disable Settings tab → Show error

#### UC-A14: Reorder Navigation Tabs
- **Actor**: Admin
- **Precondition**: On Navigation Management screen
- **Flow**:
    1. Admin navigates to Navigation Management
    2. Admin taps "Move Up" or "Move Down" on tab
    3. System calls `updateTabOrder(tabId, newOrder)`
    4. System recalculates all tab orders
    5. System updates UI to reflect new order
    6. System saves configuration
- **Postcondition**: Tab order is updated

#### UC-A15: Edit Tab Name
- **Actor**: Admin
- **Precondition**: On Navigation Management screen
- **Flow**:
    1. Admin navigates to Navigation Management
    2. Admin taps "Edit" on tab
    3. System opens edit modal
    4. Admin enters new tab name
    5. Admin taps "Save"
    6. System validates input (not empty)
    7. System calls `updateTabName(tabId, name)`
    8. System closes modal
    9. System updates display
    10. System shows success message
- **Postcondition**: Tab name is updated

#### UC-A16: Add Custom Tab
- **Actor**: Admin
- **Precondition**: On Navigation Management screen
- **Flow**:
    1. Admin navigates to Navigation Management
    2. Admin taps "Add Custom Tab"
    3. System opens add modal
    4. Admin enters Tab ID
    5. Admin enters Tab Name
    6. Admin enters Tab Icon
    7. Admin taps "Add"
    8. System validates input (ID unique, fields not empty)
    9. System calls `addCustomTab(tab)`
    10. System closes modal
    11. System updates display
    12. System shows success message
- **Postcondition**: New custom tab is added
- **Alternative Flows**:
    - Duplicate ID → Show error
    - Empty fields → Show error

#### UC-A17: Remove Custom Tab
- **Actor**: Admin
- **Precondition**: On Navigation Management screen
- **Flow**:
    1. Admin navigates to Navigation Management
    2. Admin taps "Delete" on custom tab
    3. System shows confirmation dialog
    4. Admin confirms deletion
    5. System calls `removeCustomTab(tabId)`
    6. System updates configuration
    7. System updates display
    8. System shows success message
- **Postcondition**: Custom tab is removed
- **Alternative Flows**:
    - Try to remove system tab → No delete button shown

---

## Storage Architecture

### AsyncStorage Keys

| Key | Type | Description | Scope |
|-----|------|-------------|-------|
| `@user_data` | User | Current authenticated user | Global |
| `@session_data` | Session | Last activity timestamp | Global |
| `@managed_users` | ManagedUser[] | All managed users | Admin |
| `@admin_config` | AdminConfig | System configuration | Admin |
| `@user_theme` | Theme | Light/Dark theme | Global |
| `@user_language_{userId}` | Language | User language preference | Per User |
| `@user_timezone_{userId}` | string | User timezone | Per User |
| `@user_dateformat_{userId}` | DateFormat | User date format | Per User |

---

## Security Considerations

### Access Control
1. **Role-Based Access**: Admin routes check `user.role === 'admin'`
2. **Route Protection**: Authenticated routes redirect to login
3. **Session Management**: Automatic logout on timeout
4. **User Status**: Disabled users blocked during authentication

### Data Validation
1. **Username**: Length validation (min/max)
2. **Avatar**: Size and format validation
3. **Session**: Time range validation
4. **Tab IDs**: Uniqueness validation

### Storage Security
1. **AsyncStorage**: Unencrypted (consider SecureStore for sensitive data)
2. **Session Token**: Last activity timestamp only
3. **Password**: Mock system only (no real passwords stored)

---

## Testing References

For comprehensive testing documentation, see:
- `TEST_CASES.md`: All test cases with expected behaviors
- `TESTID_REFERENCE.md`: TestID catalog for automated testing
- `TESTING_QUICKSTART.md`: Quick start guide for testing
- `TESTING_SUMMARY.md`: Testing strategy and coverage

---

## Related Documentation

- `NAVIGATION_FLOW.md`: Complete navigation flow documentation
- `NAVIGATION_MANAGEMENT_TECHNICAL.md`: Technical navigation implementation
- `README.md`: Project overview and setup instructions

---

## Version History

- **v1.0**: Initial documentation
    - Complete settings functionality for all roles
    - All use cases documented
    - API reference included
    - Navigation flow mapped
