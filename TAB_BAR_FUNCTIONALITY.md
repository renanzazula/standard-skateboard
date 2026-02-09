# Tab Bar Functionality Documentation

## Overview

This document provides a comprehensive overview of the tab bar navigation system in the application, including role-based access control, functionality of each tab, use cases, and technical implementation details.

## Table of Contents

1. [Role-Based Access Control](#role-based-access-control)
2. [Tab Bar Architecture](#tab-bar-architecture)
3. [Available Tabs](#available-tabs)
4. [Role-Specific Functionality](#role-specific-functionality)
5. [Use Cases by Role](#use-cases-by-role)
6. [API and Context Integration](#api-and-context-integration)
7. [Navigation Management](#navigation-management)

---

## Role-Based Access Control

The application supports three user roles with different levels of access:

Role values used by the backend: `GUEST`, `USER`, `ADMIN`.

### 1. Guest (Unauthenticated)
- **Status**: Not logged in
- **Access Level**: Restricted
- **Visible Tabs**: None (redirected to login)
- **Capabilities**: Cannot access any tab until authenticated

### 2. Standard User (Authenticated)
- **Status**: Logged in with role='USER'
- **Access Level**: Basic
- **Visible Tabs**: Tabs enabled by admin in Navigation Management
- **Capabilities**:
    - View and interact with enabled tabs
    - Access personal profile settings
    - Change language, theme, and preferences
    - Update username and avatar (if allowed by profile restrictions)

### 3. Admin (Authenticated)
- **Status**: Logged in with role='ADMIN'
- **Access Level**: Full
- **Visible Tabs**: All enabled tabs + Settings (always visible)
- **Capabilities**:
    - All standard user capabilities
    - Access administrative settings
    - Manage system configuration
    - Control authentication methods
    - Manage user accounts
    - Configure navigation tabs
    - Set language and regional settings
    - Define profile restrictions

---

## Tab Bar Architecture

### Technical Implementation

**File**: `app/(tabs)/_layout.tsx`

The tab bar uses Expo Router's file-based routing system with dynamic tab visibility based on:
1. Admin configuration stored in `AdminConfigContext`
2. User role from `AuthContext`
3. Tab enable/disable state
4. Tab ordering (configurable by admin)

### Tab Configuration Structure

```typescript
interface NavigationTab {
  id: string;           // Unique identifier (e.g., 'index', 'feed')
  name: string;         // Display name shown in tab bar
  enabled: boolean;     // Whether tab is visible to standard users
  icon: string;         // Icon identifier (e.g., 'Home', 'Rss')
  order: number;        // Display order (0-4)
  isSystem: boolean;    // Whether tab is system-defined (true) or custom (false)
}
```

### Default Tab Configuration

```javascript
tabs: [
  { id: 'index', name: 'Home', enabled: false, icon: 'Home', order: 0, isSystem: true },
  { id: 'feed', name: 'Feed', enabled: false, icon: 'Rss', order: 1, isSystem: true },
  { id: 'skate-square', name: 'Skate Square', enabled: false, icon: 'Droplet', order: 2, isSystem: true },
  { id: 'podcast', name: 'Podcast', enabled: false, icon: 'Mic', order: 3, isSystem: true },
  { id: 'settings', name: 'Settings', enabled: true, icon: 'Settings', order: 4, isSystem: true }
]
```

### Visibility Logic

**For Standard Users**:
```typescript
const isTabVisible = (tabId: string) => {
  const tab = config.navigationConfig.tabs.find((t) => t.id === tabId);
  return tab && tab.enabled;
}
```

**For Admin Users**:
```typescript
const isTabVisible = (tabId: string) => {
  const tab = config.navigationConfig.tabs.find((t) => t.id === tabId);
  if (!tab) return false;
  if (isAdminUser && tabId === 'settings') return true; // Settings always visible to admin
  return tab.enabled;
}
```

---

## Available Tabs

### 1. Home Tab

**File**: `app/(tabs)/index.tsx`  
**Route**: `/`  
**Icon**: Home  
**Default State**: Disabled

#### Functionality
- Displays personalized welcome message with user's name
- Shows profile information card with:
    - Email address
    - Role badge (ADMIN/USER)
    - Authentication provider (Google/Apple/Manual)
    - Current language with flag
- Admin-only section showing administrative privileges
- Quick actions for dashboard and activity tracking
- Tracks user activity for session management

#### Features
- Real-time theme switching
- Multi-language support
- User activity tracking (extends session on interaction)
- Role-based content display

#### Use Cases

**Standard User**:
- View personal profile summary
- Check current language setting
- Access dashboard overview
- Monitor activity status

**Admin User**:
- All standard user features
- View admin privilege indicator
- Quick access to administrative functions

#### Context Used
- `AuthContext`: user data, updateActivity()
- `PreferencesContext`: colors, language
- `useTranslation`: localized strings

---

### 2. Feed Tab

**File**: `app/(tabs)/feed.tsx`  
**Route**: `/feed`  
**Icon**: RSS  
**Default State**: Disabled

#### Functionality
- Content feed display (placeholder implementation)
- Shows "Coming Soon" message with description
- Displays planned features:
    - Trending content
    - Social interactions
    - Favorites management
    - Updates feed

#### Features
- Responsive layout
- Theme-aware design
- Multi-language support
- Feature preview cards

#### Use Cases

**Standard User**:
- Browse content feed
- View trending items
- Interact with social features
- Manage favorites
- Stay updated with latest content

**Admin User**:
- All standard user features
- Monitor content moderation (future feature)
- Manage feed settings (future feature)

#### Context Used
- `PreferencesContext`: colors, theme
- `useTranslation`: localized strings

---

### 3. Skate Square Tab

**File**: `app/(tabs)/skate-square.tsx`  
**Route**: `/skate-square`  
**Icon**: Droplet (Flame icon displayed)  
**Default State**: Disabled

#### Functionality
- Dedicated section for skateboarding content
- Placeholder implementation with:
    - Welcome header with icon
    - About section
    - Coming soon features

#### Features
- Scrollable content area
- Custom branding with Flame icon
- Theme integration
- Card-based layout

#### Use Cases

**Standard User**:
- Access skate-related content
- View community features
- Participate in skate square activities

**Admin User**:
- All standard user features
- Manage skate square content (future feature)
- Moderate community interactions (future feature)

#### Context Used
- `PreferencesContext`: colors

---

### 4. Podcast Tab

**File**: `app/(tabs)/podcast.tsx`  
**Route**: `/podcast`  
**Icon**: Mic  
**Default State**: Disabled

#### Functionality
- Podcast content section (placeholder implementation)
- Simple centered layout with title and subtitle
- Prepared for podcast player integration

#### Features
- Clean, minimal design
- Header configuration via Stack.Screen
- Theme-aware styling

#### Use Cases

**Standard User**:
- Browse podcast library
- Play podcast episodes
- Manage subscriptions
- Download episodes for offline listening

**Admin User**:
- All standard user features
- Upload and manage podcast content
- Configure podcast settings
- Monitor listening analytics

#### Context Used
- `PreferencesContext`: colors
- `Stack.Screen`: header customization

---

### 5. Settings Tab

**File**: `app/(tabs)/settings.tsx`  
**Route**: `/settings`  
**Icon**: Settings  
**Default State**: Enabled (Always visible to admin)

#### Functionality

**For All Users**:
- Profile management with avatar upload
    - Take photo with camera
    - Choose from gallery
    - Display current avatar or placeholder
- Username editing with validation
    - Min/max length enforcement
    - Real-time validation feedback
- Email and provider display (read-only)
- Language selection with modal picker
- Theme toggle (Dark/Light mode)
- Logout functionality

**Admin-Only Features**:
- User Management link
- Configure Authentication link
- Session Configuration link
- Language Settings link (admin-level)
- Profile Restrictions link
- Navigation Management link

#### Features
- Avatar upload with ImagePicker
    - Camera capture
    - Gallery selection
    - Image editing (crop, aspect ratio)
- Username validation
    - Length constraints
    - Real-time error messages
- Language selector modal
    - Flag display
    - Native name display
    - Available languages filtering
- Theme switcher with visual feedback
- Confirmation dialogs for critical actions

#### Use Cases

**Standard User**:
1. **Update Profile**:
    - Change username (within restrictions)
    - Upload/change avatar (within size/format limits)
    - View account information

2. **Personalize Experience**:
    - Switch between light/dark theme
    - Change language from available options
    - View current preferences

3. **Manage Session**:
    - Logout securely
    - View last activity status (implicit)

**Admin User**:
1. **All Standard User Features**

2. **User Management**:
    - View all registered users
    - Enable/disable user accounts
    - Monitor user activity
    - Change user roles

3. **Authentication Configuration**:
    - Enable/disable Google authentication
    - Enable/disable Apple authentication
    - Enable/disable Manual authentication
    - Toggle between mock/real service modes
    - Configure email verification requirements

4. **Session Configuration**:
    - Set maximum session time (default: 30 minutes)
    - Configure idle timeout (default: 15 minutes)
    - Enable/disable auto-refresh on activity

5. **Language Settings**:
    - Enable/disable available languages
    - Set default system language
    - Manage language list for all users

6. **Profile Restrictions**:
    - Set username min/max length (default: 3-30 chars)
    - Configure avatar max file size (default: 5MB)
    - Define allowed avatar formats (PNG, JPEG, JPG, SVG)

7. **Navigation Management**:
    - Enable/disable tabs for standard users
    - Rename tabs
    - Reorder tab positions
    - Add custom tabs (future feature)
    - Remove custom tabs (future feature)

#### Context Used
- `AuthContext`: user, logout, updateProfile
- `PreferencesContext`: colors, theme, language, toggleTheme, setLanguage
- `AdminConfigContext`: config (for validation rules)
- `useTranslation`: localized strings
- `useRouter`: navigation to admin pages

---

## Role-Specific Functionality

### Guest Flow

```
1. App Launch
   ↓
2. Redirect to /login (no tab access)
   ↓
3. Authentication required
```

**Available Actions**:
- Sign up with email/password
- Login with Google (if enabled)
- Login with Apple (if enabled)
- Login with email/password (if enabled)
- Forgot password flow

**No Tab Access**: Guests cannot access any tabs until authenticated.

---

### Standard User Flow

```
1. Successful Login
   ↓
2. Load User Preferences (language, theme, timezone)
   ↓
3. Load Navigation Configuration
   ↓
4. Display Enabled Tabs Only
   ↓
5. Default: Navigate to first enabled tab or Settings
```

**Tab Visibility**:
- Only tabs with `enabled: true` in admin config
- Settings tab always visible
- Tabs displayed in order defined by admin

**Restrictions**:
- Cannot access admin configuration pages
- Cannot modify system settings
- Cannot manage other users
- Profile changes limited by admin-defined restrictions

**Typical Session**:
```
Home Tab → View profile info
  ↓
Feed Tab → Browse content (if enabled)
  ↓
Settings → Change theme/language
  ↓
Settings → Update username/avatar
  ↓
Logout
```

---

### Admin User Flow

```
1. Admin Login
   ↓
2. Load Admin Configuration
   ↓
3. Load All Navigation Tabs
   ↓
4. Display All Enabled Tabs + Settings (always)
   ↓
5. Access to Admin Configuration Panel
```

**Tab Visibility**:
- All enabled tabs visible
- Settings tab always visible (even if marked disabled)
- Can see/configure tabs that standard users cannot access

**Full Access**:
- All standard user features
- Administrative configuration pages
- User management capabilities
- System-wide settings control

**Typical Admin Session**:
```
Settings → Navigation Management
  ↓
Enable "Feed" tab for standard users
  ↓
Settings → User Management
  ↓
View all users, disable a specific user
  ↓
Settings → Configure Authentication
  ↓
Enable Google Sign-In, set to Real mode
  ↓
Settings → Session Configuration
  ↓
Adjust idle timeout to 30 minutes
  ↓
Home Tab → View admin privileges badge
```

---

## Use Cases by Role

### Guest Use Cases

#### UC-G1: Access Application
**Actor**: Guest  
**Precondition**: App is opened, user is not authenticated  
**Flow**:
1. Guest opens application
2. System checks authentication status
3. System redirects to `/login`
4. Guest sees login options (based on admin config)

**Expected Result**: Login screen displayed with available authentication methods

---

#### UC-G2: Attempt Tab Access
**Actor**: Guest  
**Precondition**: Guest tries to navigate to a tab route directly  
**Flow**:
1. Guest attempts to access `/`, `/feed`, or any tab
2. Auth middleware checks authentication status
3. System detects unauthenticated user
4. System redirects to `/login`

**Expected Result**: Guest cannot access tabs, redirected to authentication

---

### Standard User Use Cases

#### UC-SU1: View Home Dashboard
**Actor**: Standard User  
**Precondition**: User is authenticated, Home tab is enabled  
**Flow**:
1. User navigates to Home tab
2. System loads user profile data from AuthContext
3. System displays personalized greeting
4. System shows profile card with email, role, provider, language
5. User can interact with quick action buttons
6. System tracks activity for session management

**Expected Result**: Personalized home dashboard displayed with user information

**API Used**:
- `useAuth()`: user, updateActivity
- `usePreferences()`: colors, language
- `useTranslation()`: t()

---

#### UC-SU2: Browse Feed Content
**Actor**: Standard User  
**Precondition**: User is authenticated, Feed tab is enabled  
**Flow**:
1. User taps Feed tab in tab bar
2. System navigates to `/feed`
3. System loads feed content (currently placeholder)
4. User views planned features and coming soon message
5. User can scroll through feature cards

**Expected Result**: Feed screen displayed with placeholder content

**API Used**:
- `usePreferences()`: colors
- `useTranslation()`: t()

---

#### UC-SU3: Change Language
**Actor**: Standard User  
**Precondition**: User is authenticated, in Settings tab  
**Flow**:
1. User taps Settings tab
2. User scrolls to Profile Settings section
3. User taps language selector
4. System opens language modal
5. System displays only languages enabled by admin
6. User selects a language
7. System saves preference to AsyncStorage
8. System updates UI with new language
9. Modal closes automatically

**Expected Result**: App language changed, all text updated to selected language

**API Used**:
- `usePreferences()`: language, setLanguage
- `useAdminConfig()`: config.languageConfig.availableLanguages
- `useTranslation()`: t()
- `AsyncStorage`: Language persisted per user

---

#### UC-SU4: Toggle Theme
**Actor**: Standard User  
**Precondition**: User is authenticated, in Settings tab  
**Flow**:
1. User navigates to Settings → Appearance section
2. User taps Dark Mode toggle switch
3. System invokes toggleTheme()
4. System saves new theme to AsyncStorage
5. System updates color scheme immediately
6. All screens reflect new theme

**Expected Result**: Theme switched between light/dark, persisted for next session

**API Used**:
- `usePreferences()`: theme, toggleTheme, colors
- `AsyncStorage`: Theme persisted globally

---

#### UC-SU5: Update Username
**Actor**: Standard User  
**Precondition**: User is authenticated, in Settings tab  
**Flow**:
1. User taps Edit button next to username
2. System opens username modal with current value
3. User enters new username
4. User taps Save button
5. System validates username length against profileConfig
6. If valid: System updates user profile, saves to AsyncStorage
7. If invalid: System shows error alert with constraints
8. Modal closes on success

**Expected Result**: Username updated if valid, error message if invalid

**Validation Rules**:
- Minimum length: `config.profileConfig.usernameMinLength` (default: 3)
- Maximum length: `config.profileConfig.usernameMaxLength` (default: 30)

**API Used**:
- `useAuth()`: user, updateProfile
- `useAdminConfig()`: config.profileConfig
- `useTranslation()`: t()
- `Alert`: Error/success feedback

---

#### UC-SU6: Upload Avatar
**Actor**: Standard User  
**Precondition**: User is authenticated, in Settings tab  
**Flow**:
1. User taps on avatar/profile header in Settings
2. System shows action sheet with options:
    - Take Photo
    - Choose from Gallery
    - Cancel
3. User selects "Take Photo":
   a. System requests camera permission
   b. If granted: Opens camera with editing enabled
   c. User takes photo, crops to 1:1 aspect ratio
   d. System saves URI, updates profile
4. User selects "Choose from Gallery":
   a. System requests media library permission
   b. If granted: Opens photo picker with editing
   c. User selects image, crops to 1:1 aspect ratio
   d. System saves URI, updates profile
5. System displays success alert

**Expected Result**: Avatar updated with new image, displayed in profile

**Restrictions**:
- Max file size: `config.profileConfig.avatarMaxSizeMB` (default: 5MB)
- Allowed formats: `config.profileConfig.allowedAvatarFormats` (PNG, JPEG, JPG, SVG)
- Aspect ratio: 1:1 (enforced by ImagePicker)

**API Used**:
- `ImagePicker.requestCameraPermissionsAsync()`
- `ImagePicker.launchCameraAsync()`
- `ImagePicker.requestMediaLibraryPermissionsAsync()`
- `ImagePicker.launchImageLibraryAsync()`
- `useAuth()`: updateProfile
- `Alert`: Feedback

---

#### UC-SU7: Logout
**Actor**: Standard User  
**Precondition**: User is authenticated  
**Flow**:
1. User navigates to Settings tab
2. User scrolls to Account section
3. User taps Logout button
4. System shows confirmation dialog
5. User confirms logout
6. System calls logout() from AuthContext
7. System clears session from AsyncStorage
8. System clears session timeout
9. System navigates to `/login`
10. Tab bar becomes inaccessible

**Expected Result**: User logged out, session cleared, redirected to login

**API Used**:
- `useAuth()`: logout
- `useRouter()`: replace('/login')
- `AsyncStorage`: Session data cleared

---

### Admin Use Cases

#### UC-A1: Manage Navigation Tabs
**Actor**: Admin  
**Precondition**: Admin is authenticated  
**Flow**:
1. Admin navigates to Settings → Admin Configuration
2. Admin taps "Navigation Management"
3. System navigates to `/navigation-management`
4. System displays list of all tabs with controls:
    - Toggle to enable/disable
    - Text input to rename
    - Up/down arrows to reorder
5. Admin disables "Podcast" tab:
   a. Admin taps toggle switch for Podcast
   b. System calls toggleTabEnabled('podcast')
   c. System updates config in AsyncStorage
   d. System immediately hides Podcast from standard users' tab bars
6. Admin renames "Feed" to "News":
   a. Admin taps edit icon for Feed
   b. Admin enters "News"
   c. System calls updateTabName('feed', 'News')
   d. System saves to config
   e. Tab displays as "News" for all users
7. Admin reorders tabs:
   a. Admin taps down arrow on "Home" (order 0)
   b. System calls updateTabOrder('index', 1)
   c. System recalculates all tab orders
   d. Tabs reorder in tab bar for all users

**Expected Result**: Tab visibility, names, and order updated for all users in real-time

**API Used**:
- `useAdminConfig()`: config, toggleTabEnabled, updateTabName, updateTabOrder
- `AsyncStorage`: Navigation config persisted

---

#### UC-A2: Configure Authentication Methods
**Actor**: Admin  
**Precondition**: Admin is authenticated  
**Flow**:
1. Admin navigates to Settings → Configure Authentication
2. System displays authentication method cards:
    - Google Sign-In
    - Apple Sign-In
    - Manual Email/Password
3. For each method, admin sees:
    - Enable/disable toggle
    - Service mode selector (Mock/Real)
    - Email verification toggle (if applicable)
4. Admin enables Google Sign-In:
   a. Admin toggles Google authentication ON
   b. System calls toggleAuthMethod('google')
   c. Google button appears on login/signup screens
5. Admin sets Google to Real mode:
   a. Admin taps "Real" in mode selector
   b. System calls setServiceMode('google', 'real')
   c. System updates config
   d. Next Google login attempts will use real OAuth
6. Admin disables Manual authentication:
   a. Admin toggles Manual OFF
   b. Email/password form hidden on login screen
   c. Standard users cannot use email/password

**Expected Result**: Authentication methods updated, login/signup screens reflect changes immediately

**API Used**:
- `useAdminConfig()`: config, toggleAuthMethod, setServiceMode
- `AsyncStorage`: Auth config persisted
- Login/Signup screens: Read enabledAuthMethods dynamically

---

#### UC-A3: Configure Session Settings
**Actor**: Admin  
**Precondition**: Admin is authenticated  
**Flow**:
1. Admin navigates to Settings → Session Configuration
2. System displays session controls:
    - Maximum session time (slider or buttons)
    - Idle timeout (slider or buttons)
    - Auto-refresh toggle
3. Admin sets idle timeout to 30 minutes:
   a. Admin uses controls to adjust value
   b. System calls updateSessionConfig({ idleTime: 30 * 60 * 1000 })
   c. System saves to config
   d. All active sessions use new timeout
4. Admin enables auto-refresh:
   a. Admin toggles auto-refresh ON
   b. System updates sessionConfig.autoRefresh = true
   c. User activity now extends sessions automatically

**Expected Result**: Session timeouts updated, affects all users immediately

**Default Values**:
- Max session time: 30 minutes (1,800,000 ms)
- Idle timeout: 15 minutes (900,000 ms)
- Auto-refresh: true

**API Used**:
- `useAdminConfig()`: config, updateSessionConfig
- `AuthContext`: Uses sessionConfig for timeout logic
- `AsyncStorage`: Session config persisted

---

#### UC-A4: Manage Languages
**Actor**: Admin  
**Precondition**: Admin is authenticated  
**Flow**:
1. Admin navigates to Settings → Language Settings
2. System displays all languages with:
    - Flag, name, native name
    - Enable/disable toggle
    - "Set Default" button
    - Default badge if currently default
3. Admin enables Spanish:
   a. Admin toggles Spanish ON
   b. System calls toggleLanguageAvailability('es')
   c. Spanish appears in language selector for all users
4. Admin sets Spanish as default:
   a. Admin taps "Set Default" for Spanish
   b. System calls setDefaultLanguage('es')
   c. New users see Spanish by default
   d. Default badge moves to Spanish
5. Admin attempts to disable default language:
   a. System prevents action
   b. Error message: "Cannot remove default language"

**Expected Result**: Available languages updated, default language set, users can select from enabled languages

**Constraints**:
- At least one language must be enabled
- Cannot disable the current default language
- Cannot set disabled language as default

**Available Languages**:
- English (en)
- Spanish (es)

**API Used**:
- `useAdminConfig()`: config, toggleLanguageAvailability, setDefaultLanguage
- `PreferencesContext`: Reads availableLanguages filter
- `AsyncStorage`: Language config persisted

---

#### UC-A5: Set Profile Restrictions
**Actor**: Admin  
**Precondition**: Admin is authenticated  
**Flow**:
1. Admin navigates to Settings → Profile Restrictions
2. System displays restriction controls:
    - Username min length (stepper/input)
    - Username max length (stepper/input)
    - Avatar max file size (MB) (stepper/input)
    - Allowed avatar formats (checkboxes)
3. Admin sets username min to 5 characters:
   a. Admin adjusts control to 5
   b. System calls updateProfileConfig({ usernameMinLength: 5 })
   c. Config saved
   d. Username validation now requires 5+ chars
4. Admin sets avatar max size to 2MB:
   a. Admin adjusts control to 2
   b. System updates avatarMaxSizeMB = 2
   c. Users cannot upload avatars > 2MB
5. Admin disables SVG format:
   a. Admin unchecks SVG in formats list
   b. System removes 'image/svg+xml' from allowedAvatarFormats
   c. SVG uploads now rejected

**Expected Result**: Profile restrictions updated, enforced for all standard users

**Default Restrictions**:
- Username min: 3 characters
- Username max: 30 characters
- Avatar max size: 5 MB
- Allowed formats: PNG, JPEG, JPG, SVG

**API Used**:
- `useAdminConfig()`: config, updateProfileConfig
- Settings screen: Reads profileConfig for validation
- `AsyncStorage`: Profile config persisted

---

#### UC-A6: Manage Users
**Actor**: Admin  
**Precondition**: Admin is authenticated  
**Flow**:
1. Admin navigates to Settings → User Management
2. System navigates to `/user-management`
3. System loads all users from UserManagementContext
4. System displays user list with:
    - Name, email, role
    - Status (Active/Disabled)
    - Provider (Google/Apple/Manual)
    - Action buttons (Disable/Enable, Change Role)
5. Admin disables a user:
   a. Admin taps "Disable" for user
   b. System shows confirmation dialog
   c. Admin confirms
   d. System calls disableUser(userId)
   e. User marked as disabled in AsyncStorage
   f. User's next login attempt is blocked
   g. If user has active session, they can continue until timeout
6. Admin changes user role to ADMIN:
   a. Admin taps "Change Role" for user
   b. System shows role selector
   c. Admin selects "Admin"
   d. System calls changeUserRole(userId, 'ADMIN')
   e. User now has admin privileges on next login

**Expected Result**: User status and roles updated, changes take effect immediately or on next login

**API Used**:
- `useUserManagement()`: users, disableUser, enableUser, changeUserRole
- `AuthContext`: checkUserStatus on login
- `AsyncStorage`: Users data persisted

---

## API and Context Integration

### AuthContext API

**File**: `contexts/AuthContext.tsx`

**State**:
```typescript
{
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  lastActivity: number
}
```

**Methods**:
```typescript
loginWithCredentials(email: string, password: string): Promise<boolean>
loginWithGoogle(): Promise<boolean>
loginWithApple(): Promise<boolean>
signUp(email: string, password: string, name: string, provider): Promise<boolean>
logout(): Promise<void>
resetPassword(email: string): Promise<boolean>
updateActivity(): void
updateProfile(updates: Partial<Pick<User, 'username' | 'avatar'>>): Promise<void>
```

**Usage in Tabs**:
- Home: Display user info, track activity
- Settings: Display profile, logout, update profile
- All tabs: Can call updateActivity() on interaction

**Storage Keys**:
- `@user_data`: Current user object
- `@session_data`: Session timestamp
- `@managed_users`: List of all users (for admin)

---

### AdminConfigContext API

**File**: `contexts/AdminConfigContext.tsx`

**State**:
```typescript
{
  config: AdminConfig
  isLoading: boolean
}
```

**Methods**:
```typescript
toggleAuthMethod(method: AuthMethod): void
setServiceMode(method: AuthMethod, mode: ServiceMode): void
updateSessionConfig(sessionConfig: Partial<SessionConfig>): void
toggleLanguageAvailability(language: Language): void
setDefaultLanguage(language: Language): void
updateRegionalConfig(regionalConfig: Partial<RegionalConfig>): void
updateProfileConfig(profileConfig: Partial<ProfileConfig>): void
toggleTabEnabled(tabId: string): void
updateTabName(tabId: string, name: string): void
addCustomTab(tab: Omit<NavigationTab, 'isSystem' | 'order'>): void
removeCustomTab(tabId: string): void
updateTabOrder(tabId: string, newOrder: number): void
```

**Usage in Tabs**:
- Tab Layout: Read navigationConfig.tabs for rendering
- Settings: Read profileConfig for validation
- Settings (admin): Access all config management methods
- Admin pages: Full CRUD operations on config

**Storage Key**:
- `@admin_config`: Complete admin configuration object

---

### PreferencesContext API

**File**: `contexts/PreferencesContext.tsx`

**State**:
```typescript
{
  theme: Theme
  colors: ThemeColors
  language: Language
  timezone: string
  dateFormat: DateFormat
  isLoading: boolean
}
```

**Methods**:
```typescript
setTheme(newTheme: Theme): Promise<void>
toggleTheme(): void
setLanguage(newLanguage: Language): Promise<void>
setTimezone(newTimezone: string): Promise<void>
setDateFormat(newDateFormat: DateFormat): Promise<void>
loadUserPreferences(userId, defaultLang?, defaultTZ?, defaultDF?): Promise<void>
clearUserPreferences(): Promise<void>
```

**Usage in Tabs**:
- All tabs: Access colors for theming
- Settings: Theme toggle, language selector
- Tab Layout: Apply theme to tab bar

**Storage Keys**:
- `@user_theme`: Global theme (light/dark)
- `@user_language_{userId}`: Per-user language
- `@user_timezone_{userId}`: Per-user timezone
- `@user_dateformat_{userId}`: Per-user date format

---

### useTranslation Hook

**File**: `hooks/useTranslation.ts`

**Returns**:
```typescript
{
  t: (key: string) => string
}
```

**Usage**:
```typescript
const { t } = useTranslation();
<Text>{t('home.welcomeBack')}</Text>
```

**Translation Files**:
- `locales/en.ts`: English translations
- `locales/es.ts`: Spanish translations

---

## Navigation Management

### Tab Enabling Flow

```
Admin Action (Settings → Navigation Management)
  ↓
toggleTabEnabled(tabId)
  ↓
AdminConfigContext updates config.navigationConfig.tabs
  ↓
Save to AsyncStorage
  ↓
Tab Layout re-renders (useAdminConfig dependency)
  ↓
isTabVisible() recalculates for each tab
  ↓
Standard users see/don't see tab in tab bar
```

### Tab Ordering Flow

```
Admin Action (Settings → Navigation Management → Reorder)
  ↓
updateTabOrder(tabId, newOrder)
  ↓
AdminConfigContext recalculates all tab orders
  ↓
Save to AsyncStorage
  ↓
Tab Layout sorts tabs by order property
  ↓
All users see tabs in new order
```

### Session Activity Flow

```
User interacts with any tab
  ↓
Tab calls updateActivity() (if implemented)
  ↓
AuthContext updates lastActivity timestamp
  ↓
Save to AsyncStorage (session_data)
  ↓
Session timeout resets (if autoRefresh enabled)
  ↓
User session extended
```

### Authentication Gating

```
User navigates to tab
  ↓
Expo Router checks if user is authenticated
  ↓
If not authenticated → Redirect to /login
  ↓
If authenticated → Check tab visibility
  ↓
If tab enabled (or admin + settings) → Render tab
  ↓
If tab disabled → Tab not in tab bar (inaccessible)
```

---

## Summary

This tab bar system provides a flexible, role-based navigation structure with:

- **3 user roles**: Guest (no access), Standard (basic access), Admin (full access)
- **5 system tabs**: Home, Feed, Skate Square, Podcast, Settings
- **Dynamic visibility**: Admin controls which tabs standard users can see
- **Extensive customization**: Tab names, order, and availability configurable
- **Session management**: Automatic timeout with activity tracking
- **Theme support**: Light/dark themes with instant switching
- **Multi-language**: English and Spanish with admin-controlled availability
- **Profile management**: Username and avatar with admin-defined restrictions
- **Persistent state**: All settings and preferences saved to AsyncStorage

The architecture ensures that:
- Guests cannot access any tabs without authentication
- Standard users only see enabled tabs
- Admins always have access to Settings and configuration
- All configuration changes take effect immediately
- User preferences are persisted across sessions
- Role-based access is enforced at the context level
