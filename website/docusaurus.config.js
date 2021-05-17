/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'sttp-oauth2',
  tagline: 'OAuth2 client library for Scala',
  url: 'https://ocadotechnology.github.io',
  baseUrl: '/sttp-oauth2/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/logo.png',
  organizationName: 'ocadotechnology', // Usually your GitHub org/user name.
  projectName: 'sttp-oauth2', // Usually your repo name.
  themeConfig: {
    image: "img/ocado.png",
    defaultMode: 'dark',
    prism: {
      // Java is here due to https://github.com/facebook/docusaurus/issues/4799
      additionalLanguages: ['java', 'scala'],
      theme: require('prism-react-renderer/themes/vsDark')
    },
    navbar: {
      title: 'sttp-oauth2',
      logo: {
        alt: 'sttp-oauth2',
        src: 'img/logo.png',
      },
      items: [
        {
          type: 'doc',
          docId: 'getting-started',
          position: 'left',
          label: 'Documentation',
        },
        // {to: '/blog', label: 'Blog', position: 'left'},
        {
          href: 'https://github.com/ocadotechnology/sttp-oauth2',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Documentation',
              to: '/docs/getting-started',
            },
            {
              label: 'Authorization code grant',
              to: '/docs/authorization-code',
            },
            {
              label: 'Client credentails grant',
              to: '/docs/client-credentials',
            },
            {
              label: 'Password grant',
              to: '/docs/password-grant',
            },
          ],
        },
        // {
        //   title: 'Community',
        //   items: [
        //     {
        //       label: 'Stack Overflow',
        //       href: 'https://stackoverflow.com/questions/tagged/docusaurus',
        //     },
        //     {
        //       label: 'Discord',
        //       href: 'https://discordapp.com/invite/docusaurus',
        //     },
        //     {
        //       label: 'Twitter',
        //       href: 'https://twitter.com/docusaurus',
        //     },
        //   ],
        // },
        {
          title: 'More',
          items: [
            {
              label: 'Project on GitHub',
              href: 'https://github.com/ocadotechnology/sttp-oauth2',
            },
            {
              label: 'Ocado Technology on GitHub',
              href: 'https://github.com/ocadotechnology',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Ocado Technology`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          path: "../mdoc/target/mdoc",
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: ({locale, versionDocsDirPath, docPath}) => {
            return `https://github.com/ocadotechnology/sttp-oauth2/edit/master/docs/${docPath}`;
          }
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          editUrl:
            'https://github.com/ocadotechnology/sttp-oauth2/edit/master/website/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};