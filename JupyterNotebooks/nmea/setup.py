from setuptools import setup

setup(
    name='NMEASerialTools',
    version='0.0.5',
    packages=[''],
    package_dir={'.': 'nmea'},
    url='',
    license='MIT',
    author='Olivier LeDiouris',
    author_email='olivier@lediouris.net',
    description='NMEA tools, read a GPS',
    install_requires=[
        'http', 'pyserial', 'serial'
    ],
    zip_safe=False
)
